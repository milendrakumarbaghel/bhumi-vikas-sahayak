package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springboot.backend.dto.AskQuestionRequest;
import org.springboot.backend.entity.*;
import org.springboot.backend.integration.ai.GroundedAiClient;
import org.springboot.backend.repository.*;
import org.springboot.backend.service.*;
import org.springboot.backend.service.impl.*;
import org.springboot.backend.util.*;
import tools.jackson.databind.json.JsonMapper;

/** Real corpus, production intent/retrieval/assistant and a live Python calculator.
 * Database ranking has separate PostgreSQL integration tests; no provider/network law is used.
 */
class OfficialSampleQuestionsTest {
    private static Process python;
    private static String baseUrl;
    private static final JsonMapper JSON=JsonMapper.builder().build();
    private static List<RulePassage> corpus;
    private static List<String> questions;

    @BeforeAll
    static void startPythonAndLoadCorpus() throws Exception {
        questions=Files.readAllLines(Path.of("../track-1-bhumi-vikas-sahayak/resources/sample_questions.md")).stream()
                .filter(line -> line.matches("\\d+\\..+")).map(line -> line.replaceFirst("^\\d+\\.\\s*","")).toList();
        assertThat(questions).hasSize(17);
        try(var input=OfficialSampleQuestionsTest.class.getResourceAsStream("/data/bhumi_vikas_rules.jsonl")) {
            var chunker=new RulePassageChunker();
            corpus=new JsonlParser(JSON).parse(input).stream().flatMap(record -> {
                var rule=new BuildingRule(record.rule(),record.title(),record.part(),record.pageStart(),record.pageEnd(),record.text());
                chunker.chunk(record.rule(),record.pageStart(),record.pageEnd(),record.text()).forEach(chunk ->
                    rule.addPassage(new RulePassage(chunk.label(),chunk.pageStart(),chunk.pageEnd(),chunk.text())));
                return rule.getPassages().stream();
            }).toList();
        }
        int port;
        try(var socket=new ServerSocket(0)) { port=socket.getLocalPort(); }
        baseUrl="http://127.0.0.1:"+port;
        String executable=System.getProperty("bvs.test.python",
                System.getProperty("os.name").toLowerCase().contains("windows")?"python":"python3");
        var builder=new ProcessBuilder(executable,"-m","ai_service").directory(Path.of("../ai-service").toFile());
        builder.environment().put("AI_SERVICE_HOST","127.0.0.1");
        builder.environment().put("AI_SERVICE_PORT",Integer.toString(port));
        builder.environment().put("ANTHROPIC_API_KEY","");
        builder.environment().put("BVS_AI_SERVICE_TOKEN","sample-test");
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD);
        python=builder.start();
        boolean ready=false;
        for(int attempt=0;attempt<100;attempt++) {
            try {
                ready=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(baseUrl+"/internal/v1/health"))
                    .timeout(Duration.ofSeconds(1)).GET().build(),HttpResponse.BodyHandlers.discarding()).statusCode()==200;
                if(ready) break;
            } catch(Exception ignored) {}
            Thread.sleep(100);
        }
        assertThat(ready).as("Python test server started").isTrue();
    }

    @AfterAll
    static void stopPython() throws Exception {
        if(python!=null) {python.destroy();if(!python.waitFor(3,java.util.concurrent.TimeUnit.SECONDS)) python.destroyForcibly();}
    }

    static Stream<Gold> officialQuestions() {
        return Stream.of(
            new Gold(1,"56","ANSWERED",List.of("3.6"),List.of()),
            new Gold(2,"17","ANSWERED",List.of("0.40","0.10"),List.of()),
            new Gold(3,"23","ANSWERED",List.of("three years","two consecutive terms","execution"),List.of()),
            new Gold(4,"28","ANSWERED",List.of("deemed to have been sanctioned","contravention","shall not be deemed"),List.of()),
            new Gold(5,"52","ANSWERED",List.of("2.5","1.2"),List.of()),
            new Gold(6,"18","ANSWERED",List.of("architect","owner/applicant","license"),List.of()),
            new Gold(7,"16","ANSWERED",List.of("building plans","proof of ownership","certificate of supervision"),List.of()),
            new Gold(8,"21","NEEDS_MORE_INFORMATION",List.of("built-up area"),List.of()),
            new Gold(9,"24","ANSWERED",List.of("No deviation","front","parking"),List.of()),
            new Gold(10,"56","ANSWERED",List.of("1.5"),List.of()),
            new Gold(11,"2","NEEDS_REVIEW",List.of("authoritative text is not loaded"),List.of()),
            new Gold(12,"103","ANSWERED",List.of("Development plan","Government","final"),List.of()),
            new Gold(13,"83","ANSWERED",List.of("12.5","National Building Code"),List.of()),
            new Gold(14,"56","ANSWERED",List.of("3.0"),List.of()),
            new Gold(15,"21","NEEDS_MORE_INFORMATION",List.of("built-up area"),List.of()),
            new Gold(16,"","NOT_FOUND_IN_RULES",List.of(),List.of()),
            new Gold(17,"","NOT_FOUND_IN_RULES",List.of(),List.of()));
    }

    @ParameterizedTest(name="Official question {0}")
    @MethodSource("officialQuestions")
    void evaluatesActualAnswerAgainstLegalGold(Gold gold) {
        var passageRepo=mock(RulePassageRepository.class);
        when(passageRepo.rankedSearch(anyString())).thenReturn(List.of());
        when(passageRepo.search(anyString())).thenAnswer(call -> {
            String term=call.<String>getArgument(0).toLowerCase();
            return corpus.stream().filter(p -> p.getText().toLowerCase().contains(term)
                || p.getRule().getTitle().toLowerCase().contains(term)||p.getRule().getRuleNumber().equalsIgnoreCase(term)).toList();
        });
        when(passageRepo.forRule(anyString())).thenAnswer(call -> corpus.stream()
                .filter(p -> p.getRule().getRuleNumber().equalsIgnoreCase(call.getArgument(0))).toList());
        var retrieval=new RuleRetrievalService(mock(BuildingRuleRepository.class),passageRepo);
        var intent=new ClaudeAiService(HttpClient.newHttpClient(),JSON,new LanguageDetector(),baseUrl,"sample-test",false,Duration.ofSeconds(2));
        var grounded=new GroundedAiClient(HttpClient.newHttpClient(),JSON,baseUrl,"sample-test",true,Duration.ofSeconds(2));
        var amendments=mock(AmendmentService.class);
        when(amendments.resolve(any(),any(),any(),any())).thenAnswer(call -> call.getArgument(0));
        when(amendments.needsAugust2026Review(any(),any())).thenAnswer(call ->
                call.<List<String>>getArgument(0).stream().anyMatch(ref -> ref.equalsIgnoreCase("2(5)(c)")));
        var service=new AssistantServiceImpl(intent,retrieval,new CalculationService(),new CitationValidationService(),
                amendments,grounded,new GeneratedAnswerValidationService(JSON));
        var answer=service.answer(new AskQuestionRequest(questions.get(gold.number()-1),null,LocalDate.of(2026,10,11)));
        assertThat(answer.status()).as("Q"+gold.number()+": "+answer).isEqualTo(gold.status());
        if(!gold.rule().isBlank()) assertThat(answer.citations()).anyMatch(c -> c.ruleNumber().equals(gold.rule()) && c.validated());
        else assertThat(answer.citations()).isEmpty();
        String content=(answer.answer()+" "+JSON.writeValueAsString(answer.calculation())+" "+String.join(" ",answer.warnings()))
                .replaceAll("\\s+"," ").toLowerCase();
        for(String expected:gold.required()) assertThat(content).as("Q"+gold.number()+" required fact").contains(expected.toLowerCase());
        for(String forbidden:gold.forbidden()) assertThat(content).doesNotContain(forbidden.toLowerCase());
        if(gold.number()==14||gold.number()==15) assertThat(answer.language()).isEqualTo("hi");
    }

    record Gold(int number,String rule,String status,List<String> required,List<String> forbidden) {
        @Override public String toString() { return Integer.toString(number); }
    }
}
