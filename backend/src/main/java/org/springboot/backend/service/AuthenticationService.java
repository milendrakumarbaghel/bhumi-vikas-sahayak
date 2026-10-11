package org.springboot.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final Duration ttl;
    private final String bootstrapUsername;
    private final String bootstrapPassword;
    private final SecureRandom random = new SecureRandom();

    public AuthenticationService(JdbcTemplate jdbc, PasswordEncoder passwords,
            @Value("${bvs.auth.token-ttl:8h}") Duration ttl,
            @Value("${bvs.auth.bootstrap-username:}") String username,
            @Value("${bvs.auth.bootstrap-password:}") String password) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.ttl = ttl;
        this.bootstrapUsername = username;
        this.bootstrapPassword = password;
        if (ttl.isNegative() || ttl.isZero() || ttl.compareTo(Duration.ofDays(1)) > 0)
            throw new IllegalArgumentException("Token lifetime must be positive and at most 24 hours");
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapUsername.isBlank() && bootstrapPassword.isBlank()) return;
        if (bootstrapUsername.isBlank() || bootstrapPassword.isBlank())
            throw new IllegalStateException("Both bootstrap admin credentials are required");
        String name = username(bootstrapUsername);
        validatePassword(bootstrapPassword);
        jdbc.update("insert into app_user(username,password_hash,role) values (?,?, 'ADMIN') on conflict (username) do nothing",
                name, passwords.encode(bootstrapPassword));
    }

    public TokenResponse login(String suppliedName, String password) {
        String name = username(suppliedName);
        var users = jdbc.query("select * from app_user where username=?", (rs, row) -> new Credentials(
                rs.getLong("id"), rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("enabled"),
                rs.getObject("locked_until", OffsetDateTime.class)), name);
        if (users.isEmpty()) {
            passwords.matches(password == null ? "" : password, DUMMY_HASH);
            throw unauthorized();
        }
        Credentials user = users.getFirst();
        if (!user.enabled() || user.lockedUntil() != null && user.lockedUntil().toInstant().isAfter(Instant.now()))
            throw unauthorized();
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !passwords.matches(password, user.hash())) {
            jdbc.update("""
                    update app_user set failed_logins=failed_logins+1,
                      locked_until=case when failed_logins>=4 then current_timestamp + interval '15 minutes' else locked_until end
                    where id=?
                    """, user.id());
            throw unauthorized();
        }
        jdbc.update("update app_user set failed_logins=0,locked_until=null where id=?", user.id());
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expires = Instant.now().plus(ttl);
        jdbc.update("insert into access_token(token_hash,user_id,expires_at) values (?,?,?)",
                hash(token), user.id(), OffsetDateTime.ofInstant(expires, java.time.ZoneOffset.UTC));
        return new TokenResponse(token, "Bearer", expires, name, user.role());
    }

    public Optional<Principal> authenticate(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return Optional.empty();
        return jdbc.query("""
                select u.id,u.username,u.role from access_token t join app_user u on u.id=t.user_id
                where t.token_hash=? and not t.revoked and t.expires_at>current_timestamp and u.enabled
                """, (rs, row) -> new Principal(rs.getLong(1), rs.getString(2), rs.getString(3)), hash(token))
                .stream().findFirst();
    }

    public void logout(String token) {
        jdbc.update("update access_token set revoked=true where token_hash=?", hash(token));
    }

    @Transactional
    public Principal createUser(String suppliedName, String password, String role) {
        String name = username(suppliedName);
        validatePassword(password);
        if (!List.of("USER", "ADMIN").contains(role))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Role must be USER or ADMIN");
        if (jdbc.queryForObject("select count(*) from app_user where username=?", Long.class, name) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        Long id = jdbc.queryForObject("insert into app_user(username,password_hash,role) values (?,?,?) returning id",
                Long.class, name, passwords.encode(password), role);
        return new Principal(id, name, role);
    }

    @Transactional
    public void disableUser(long id, long actingId) {
        if (id == actingId) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot disable your own account");
        if (jdbc.update("update app_user set enabled=false where id=?", id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        jdbc.update("update access_token set revoked=true where user_id=?", id);
    }

    private static String username(String name) {
        String value = name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9._@-]{3,100}")) throw unauthorized();
        return value;
    }
    private static void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Password must have at least 12 characters and at most 72 UTF-8 bytes");
    }
    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static final String DUMMY_HASH = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private record Credentials(long id, String hash, String role, boolean enabled, OffsetDateTime lockedUntil) {}
    public record Principal(long id, String username, String role) {}
    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt, String username, String role) {}
}
