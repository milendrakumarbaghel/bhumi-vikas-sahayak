package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springboot.backend.integration.ai.GroundedAiClient;
import tools.jackson.databind.json.JsonMapper;

class GroundedClaimVerificationTest {
    @Test
    void reversedMeaningIncompleteCoverageAndVerifierFailureNeverPass() throws Exception {
        var body=new AtomicReference<>("{\"verdicts\":[{\"id\":0,\"verdict\":\"contradicted\"}]}");
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/internal/v1/claims:verify",exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("x-internal-token")).isEqualTo("test-token");
            String request=new String(exchange.getRequestBody().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            assertThat(request).contains("Provision for lifts shall","No lift is required");
            byte[] response=body.get().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);exchange.close();
        });
        server.start();
        try {
            var client=new GroundedAiClient(HttpClient.newHttpClient(),JsonMapper.builder().build(),
                    "http://127.0.0.1:"+server.getAddress().getPort(),"test-token",true,Duration.ofSeconds(2));
            var proposal=new GroundedAiClient.Proposal("answered","en","No lift is required.",
                    List.of(new GroundedAiClient.CitedQuote("83","Provision for lifts shall be made for building more than 12.5 meters in height.")),
                    List.of(),Map.of(),"test",List.of(new GroundedAiClient.Claim("No lift is required.",List.of("83"))));
            assertThat(client.verifyClaims(proposal,Map.of(),"15 m building")).isFalse();
            body.set("{\"verdicts\":[]}");
            assertThat(client.verifyClaims(proposal,Map.of(),"15 m building")).isFalse();
            body.set("{\"verdicts\":[{\"id\":0,\"verdict\":\"entailed\"}]}");
            assertThat(client.verifyClaims(proposal,Map.of(),"15 m building")).isTrue();
            server.stop(0);
            assertThat(client.verifyClaims(proposal,Map.of(),"15 m building")).isFalse();
        } finally { server.stop(0); }
    }
}
