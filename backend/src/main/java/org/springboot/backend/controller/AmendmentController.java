package org.springboot.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springboot.backend.service.AmendmentService;
import org.springboot.backend.service.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/amendments")
public class AmendmentController {
    private final AmendmentService amendments;
    public AmendmentController(AmendmentService amendments) { this.amendments=amendments; }
    @GetMapping public List<Map<String,Object>> list() { return amendments.list(); }
    @PostMapping public Map<String,Object> ingest(@Valid @RequestBody Draft input,Authentication actor) {
        return amendments.ingest(new AmendmentService.Submission(input.ruleNumber(),input.notificationNumber(),input.effectiveDate(),
                input.replacementText(),input.sourceUrl(),input.sourceSha256(),input.pageStart(),input.pageEnd()),name(actor));
    }
    @PostMapping("/{id}/review") public Map<String,Object> review(@PathVariable long id,@Valid @RequestBody Review input,Authentication actor) {
        return amendments.review(id,name(actor),input.note());
    }
    @PostMapping("/{id}/activate") public Map<String,Object> activate(@PathVariable long id,Authentication actor) {
        return amendments.activate(id,name(actor));
    }
    @PostMapping("/{id}/reject") public Map<String,Object> reject(@PathVariable long id,@Valid @RequestBody Review input,Authentication actor) {
        return amendments.reject(id,name(actor),input.note());
    }
    @GetMapping("/{id}/history") public List<Map<String,Object>> history(@PathVariable long id) { return amendments.history(id); }
    private static String name(Authentication auth) { return ((AuthenticationService.Principal)auth.getPrincipal()).username(); }
    public record Draft(@NotBlank @Size(max=64) String ruleNumber,@NotBlank @Size(max=255) String notificationNumber,
            @NotNull LocalDate effectiveDate,@NotBlank @Size(min=12,max=12000) String replacementText,
            @NotBlank @Pattern(regexp="https://[^\\s]{1,2040}") String sourceUrl,
            @NotBlank @Pattern(regexp="[A-Fa-f0-9]{64}") String sourceSha256,@Min(1) int pageStart,@Min(1) int pageEnd) {}
    public record Review(@NotBlank @Size(min=12,max=4000) String note) {}
}
