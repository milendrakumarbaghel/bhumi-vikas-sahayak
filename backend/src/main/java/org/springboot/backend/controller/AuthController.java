package org.springboot.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springboot.backend.service.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationService auth;
    public AuthController(AuthenticationService auth) { this.auth = auth; }
    @PostMapping("/auth/login")
    AuthenticationService.TokenResponse login(@Valid @RequestBody Login request) {
        return auth.login(request.username(), request.password());
    }
    @GetMapping("/auth/me")
    AuthenticationService.Principal me(Authentication authentication) {
        return (AuthenticationService.Principal) authentication.getPrincipal();
    }
    @PostMapping("/auth/logout")
    void logout(@RequestHeader("Authorization") String bearer) { auth.logout(bearer.substring(7)); }
    @PostMapping("/admin/users")
    AuthenticationService.Principal create(@Valid @RequestBody CreateUser request) {
        return auth.createUser(request.username(), request.password(), request.role());
    }
    @PostMapping("/admin/users/{id}/disable")
    void disable(@PathVariable long id, Authentication authentication) {
        auth.disableUser(id, ((AuthenticationService.Principal) authentication.getPrincipal()).id());
    }
    public record Login(@NotBlank @Size(max=100) String username, @NotBlank @Size(max=72) String password) {}
    public record CreateUser(@NotBlank @Size(max=100) String username, @NotBlank @Size(min=12,max=72) String password,
            @Pattern(regexp="USER|ADMIN") @NotBlank String role) {}
}
