package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springboot.backend.repository.RulePassageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springboot.backend.service.AuthenticationService;
import org.springboot.backend.service.AmendmentService;
import org.springboot.backend.service.RuleRetrievalService;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"bvs.ai-service.enabled=false","debug=false","logging.level.root=WARN"})
class PostgreSqlIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    BuildingRuleRepository rules;

    @Autowired
    RulePassageRepository passages;
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthenticationService auth;
    @Autowired AmendmentService amendments;
    @Autowired RuleRetrievalService retrieval;
    @LocalServerPort int port;

    @Test
    void flywayCreatesSchemaAndStartupImportsChunkedCorpus() {
        assertThat(rules.count()).isEqualTo(106);
        assertThat(passages.count()).isGreaterThan(106);
        assertThat(rules.findByRuleNumberIgnoreCase("56")).isPresent()
                .get().satisfies(rule -> assertThat(rule.getPassages()).hasSizeGreaterThan(2));
    }

    @Test
    void fullTextRankingAndHindiExpansionFindRealClauses() {
        var ids=passages.rankedSearch("rear open space");
        assertThat(ids).isNotEmpty();
        assertThat(passages.fetchRanked(ids)).anyMatch(p -> p.getRule().getRuleNumber().equals("56"));
        assertThat(retrieval.retrieve("पीछे कितनी खाली जगह छोड़नी होगी",List.of("56")))
                .anyMatch(p -> p.getRule().getRuleNumber().equals("56") && p.getText().contains("Rear"));
    }

    @Test
    void userAndAdminTokensEnforceRolesExpiryAndLogout() throws Exception {
        String name="clerk"+System.nanoTime();
        var user=auth.createUser(name,"test-password-only-123","USER");
        var login=auth.login(name,"test-password-only-123");
        assertThat(jdbc.queryForObject("select password_hash from app_user where id=?",String.class,user.id())).startsWith("$2a$");
        assertThat(request("/api/v1/rules/imports:run","POST",null).statusCode()).isEqualTo(401);
        assertThat(request("/api/v1/rules/imports:run","POST",login.accessToken()).statusCode()).isEqualTo(403);
        assertThat(request("/api/v1/auth/me","GET",login.accessToken()).statusCode()).isEqualTo(200);
        assertThat(request("/api/v1/auth/logout","POST",login.accessToken()).statusCode()).isEqualTo(200);
        assertThat(request("/api/v1/auth/me","GET",login.accessToken()).statusCode()).isEqualTo(401);
        var second=auth.login(name,"test-password-only-123");
        jdbc.update("update access_token set expires_at=current_timestamp-interval '1 second' where user_id=?",user.id());
        assertThat(request("/api/v1/auth/me","GET",second.accessToken()).statusCode()).isEqualTo(401);
        var admin=auth.createUser("admin"+System.nanoTime(),"admin-password-only-123","ADMIN");
        var token=auth.login(admin.username(),"admin-password-only-123");
        assertThat(request("/api/v1/rules/imports:run","POST",token.accessToken()).statusCode()).isEqualTo(200);
    }

    @Test
    void independentReviewActivationSupersessionAndHistoricalResolution() {
        var first=amendments.ingest(new AmendmentService.Submission("56(3)","TEST-NOTIFICATION-1",LocalDate.of(2026,1,1),
                "TEST FIXTURE: detached buildings require 4 meters side open space.","https://example.gov/test.pdf","a".repeat(64),1,1),"maker");
        long firstId=((Number)first.get("id")).longValue();
        assertThat(amendments.applicable(LocalDate.of(2026,2,1))).isEmpty();
        assertThatThrownBy(()->amendments.activate(firstId,"maker")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->amendments.review(firstId,"maker","I checked my own draft source")).isInstanceOf(ResponseStatusException.class);
        amendments.review(firstId,"reviewer","Verified synthetic test source and exact replacement");
        amendments.activate(firstId,"reviewer");
        var second=amendments.ingest(new AmendmentService.Submission("56(3)","TEST-NOTIFICATION-2",LocalDate.of(2026,3,1),
                "TEST FIXTURE: detached buildings require 5 meters side open space.","https://example.gov/test2.pdf","b".repeat(64),2,2),"maker");
        long secondId=((Number)second.get("id")).longValue();
        amendments.review(secondId,"reviewer","Verified second synthetic test source and exact replacement");
        amendments.activate(secondId,"reviewer");
        assertThat(amendments.get(firstId)).containsEntry("status","RETIRED").containsEntry("superseded_by",secondId);
        assertThat(amendments.applicable(LocalDate.of(2026,2,1))).singleElement().satisfies(row->assertThat(row.get("id")).isEqualTo(firstId));
        assertThat(amendments.applicable(LocalDate.of(2026,3,1))).singleElement().satisfies(row->assertThat(row.get("id")).isEqualTo(secondId));
        var base=passages.forRule("56");
        assertThat(amendments.resolve(base,"side open space",List.of("56(3)"),LocalDate.of(2025,12,31)))
                .anyMatch(p->p.getText().contains("3.0 meters on one side"));
        assertThat(amendments.resolve(base,"side open space",List.of("56(3)"),LocalDate.of(2026,3,1)))
                .anyMatch(p->p.getText().contains("5 meters") && p.getSourceUrl().equals("https://example.gov/test2.pdf"))
                .noneMatch(p->p.getLabel().equals("Rule 56 (3)"));
        assertThat(amendments.history(firstId)).hasSize(4);
    }

    @Test
    void rejectedDraftCannotBeActivatedOrUsedAsEvidence() {
        var draft=amendments.ingest(new AmendmentService.Submission("42","TEST-REJECTED-42",LocalDate.of(2027,1,1),
                "TEST FIXTURE: invalid source must never become authoritative evidence.",
                "https://example.gov/rejected.pdf","c".repeat(64),1,2),"maker");
        long id=((Number)draft.get("id")).longValue();
        amendments.reject(id,"reviewer","Source failed provenance and legal wording review");
        assertThatThrownBy(()->amendments.activate(id,"reviewer")).isInstanceOf(ResponseStatusException.class);
        assertThat(amendments.applicable(LocalDate.of(2028,1,1)))
                .noneMatch(row->row.get("id").equals(id));
        assertThat(amendments.history(id)).hasSize(2);
    }

    private HttpResponse<String> request(String path,String method,String token) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path));
        if(token!=null) builder.header("Authorization","Bearer "+token);
        builder.method(method,HttpRequest.BodyPublishers.noBody());
        return HttpClient.newHttpClient().send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
}
