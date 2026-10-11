package org.springboot.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AmendmentService {
    private final JdbcTemplate jdbc;
    private final BuildingRuleRepository rules;
    public AmendmentService(JdbcTemplate jdbc, BuildingRuleRepository rules) { this.jdbc=jdbc; this.rules=rules; }

    @Transactional
    public Map<String,Object> ingest(Submission input, String actor) {
        String target=canonical(input.ruleNumber());
        if (!target.matches("[0-9]+(?:-[A-Z])?(?:\\([0-9A-Za-z]+\\))*")) bad("Invalid amendment target");
        BuildingRule base=rules.findByRuleNumberIgnoreCase(baseRule(target))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Rule not found"));
        if (!input.effectiveDate().isAfter(LocalDate.of(2025,3,6))) bad("Amendment must be effective after the loaded corpus date");
        String scope=target.substring(baseRule(target).length());
        if (!scope.isEmpty() && base.getPassages().stream().noneMatch(p -> scopeExists(p,scope))) bad("Target clause is absent from corpus");
        if(input.pageStart()<1 || input.pageEnd()<input.pageStart()) bad("Invalid source page range");
        Long id=jdbc.queryForObject("""
                insert into rule_amendment(rule_number,notification_number,effective_date,replacement_text,status,
                    source_url,source_sha256,page_start,page_end,created_by)
                values (?,?,?,?,'DRAFT',?,?,?,?,?) returning id
                """,Long.class,target,input.notificationNumber().strip(),input.effectiveDate(),input.replacementText().strip(),
                input.sourceUrl(),input.sourceSha256().toLowerCase(),input.pageStart(),input.pageEnd(),actor);
        audit(id,"INGEST",actor,"Draft submitted with source provenance");
        return get(id);
    }

    @Transactional
    public Map<String,Object> review(long id,String actor,String note) {
        var row=lock(id);
        if(!"DRAFT".equals(row.get("status"))) conflict("Only drafts can be reviewed");
        if(actor.equals(row.get("created_by"))) conflict("A different administrator must review the source and replacement");
        jdbc.update("update rule_amendment set status='REVIEWED',reviewed_by=?,review_note=?,reviewed_at=current_timestamp,version=version+1 where id=?",actor,note,id);
        audit(id,"REVIEW",actor,note);
        return get(id);
    }

    @Transactional
    public Map<String,Object> reject(long id,String actor,String note) {
        var row=lock(id);
        if(!List.of("DRAFT","REVIEWED").contains(row.get("status"))) conflict("Only unactivated amendments can be rejected");
        jdbc.update("update rule_amendment set status='RETIRED',review_note=?,version=version+1 where id=?",note,id);
        audit(id,"REJECT",actor,note);
        return get(id);
    }

    @Transactional
    public Map<String,Object> activate(long id,String actor) {
        // Lock the base rule first so simultaneous activations cannot deadlock on predecessor rows.
        var initial=get(id);
        String target=initial.get("rule_number").toString();
        jdbc.queryForList("select id from building_rule where lower(rule_number)=lower(?) for update",baseRule(target));
        var row=lock(id);
        if(!"REVIEWED".equals(row.get("status"))) conflict("Only independently reviewed amendments can be activated");
        LocalDate effective=date(row.get("effective_date"));
        var existing=jdbc.queryForList("select * from rule_amendment where status='ACTIVE' and rule_number like ? order by effective_date,id for update",baseRule(target)+"%");
        for(var prior:existing) {
            String previousTarget=prior.get("rule_number").toString();
            if(!overlaps(target,previousTarget)) continue;
            if(!target.equals(previousTarget)) conflict("Overlapping whole-rule/clause amendments require a consistent replacement scope");
            if(!effective.isAfter(date(prior.get("effective_date")))) conflict("Activation must follow the previous effective date");
            long previousId=((Number)prior.get("id")).longValue();
            jdbc.update("update rule_amendment set status='RETIRED',valid_until=?,superseded_by=?,version=version+1 where id=?",effective,id,previousId);
            audit(previousId,"SUPERSEDE",actor,"Superseded by amendment "+id+" from "+effective);
        }
        jdbc.update("update rule_amendment set status='ACTIVE',activated_by=?,activated_at=current_timestamp,version=version+1 where id=?",actor,id);
        audit(id,"ACTIVATE",actor,"Effective from "+effective);
        return get(id);
    }

    public List<Map<String,Object>> list() { return jdbc.queryForList("select * from rule_amendment order by effective_date desc,id desc"); }
    public Map<String,Object> get(long id) {
        var rows=jdbc.queryForList("select * from rule_amendment where id=?",id);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Amendment not found");
        return rows.getFirst();
    }
    public List<Map<String,Object>> history(long id) {
        get(id);return jdbc.queryForList("select * from amendment_audit where amendment_id=? order by id",id);
    }
    public List<Map<String,Object>> applicable(LocalDate asOfDate) {
        return jdbc.queryForList("""
                select * from rule_amendment where status in ('ACTIVE','RETIRED') and activated_at is not null
                and effective_date<=? and (valid_until is null or valid_until>?) order by effective_date,id
                """,asOfDate,asOfDate);
    }
    public boolean needsAugust2026Review(List<String> references,LocalDate asOfDate) {
        return !asOfDate.isBefore(LocalDate.of(2026,8,27))
                && references.stream().map(AmendmentService::canonical).anyMatch(ref -> ref.equals("2") || ref.startsWith("2(5)(C)"))
                && applicable(asOfDate).stream().noneMatch(row -> overlaps(row.get("rule_number").toString(),"2(5)(C)"));
    }

    /** Superseded text never enters current answers; original corpus remains immutable. */
    public List<RulePassage> resolve(List<RulePassage> basePassages,String query,List<String> references,LocalDate date) {
        var result=new ArrayList<>(basePassages);
        for(var row:applicable(date)) {
            String target=row.get("rule_number").toString(),rule=baseRule(target),scope=target.substring(rule.length());
            boolean relevant=result.stream().anyMatch(p -> p.getRule().getRuleNumber().equalsIgnoreCase(rule))
                    || references.stream().map(AmendmentService::canonical).anyMatch(ref -> overlaps(ref,target))
                    || org.springboot.backend.util.QueryLexicon.terms(query).stream()
                        .filter(term -> term.length()>4).filter(term -> row.get("replacement_text").toString().toLowerCase().contains(term)).count()>=2;
            if(!relevant) continue;
            result.removeIf(p -> p.getRule().getRuleNumber().equalsIgnoreCase(rule) && (scope.isEmpty() || scopeExists(p,scope)));
            int first=((Number)row.get("page_start")).intValue(),last=((Number)row.get("page_end")).intValue();
            var source=new BuildingRule(rule,"Amendment "+row.get("notification_number"),"Amendments",first,last,row.get("replacement_text").toString());
            var passage=new RulePassage("Rule "+target,first,last,row.get("replacement_text").toString());
            passage.withSource(row.get("source_url").toString(),"Notification "+row.get("notification_number")+"; effective "+row.get("effective_date"));
            source.addPassage(passage);result.addFirst(passage);
        }
        return result.stream().limit(8).toList();
    }
    private static boolean scopeExists(RulePassage passage,String scope) {
        String label=canonical(passage.getLabel().replaceFirst("(?i)^Rule\\s*",""));
        String target=canonical(passage.getRule().getRuleNumber())+scope;
        if(label.equals(target) || label.startsWith(target+"(")) return true;
        if(!target.startsWith(label+"(")) return false;
        String remainder=target.substring(label.length());
        return passage.getText().toUpperCase().contains(remainder);
    }
    private static boolean overlaps(String a,String b) { return a.equalsIgnoreCase(b)||a.toUpperCase().startsWith(b.toUpperCase()+"(")||b.toUpperCase().startsWith(a.toUpperCase()+"("); }
    public static String canonical(String value) { return value.replaceAll("\\s","").toUpperCase(java.util.Locale.ROOT); }
    private static String baseRule(String value) { int end=value.indexOf('(');return end<0?value:value.substring(0,end); }
    private static LocalDate date(Object value) { return value instanceof java.sql.Date d?d.toLocalDate():LocalDate.parse(value.toString()); }
    private Map<String,Object> lock(long id) {
        var rows=jdbc.queryForList("select * from rule_amendment where id=? for update",id);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Amendment not found");return rows.getFirst();
    }
    private void audit(long id,String action,String actor,String note) { jdbc.update("insert into amendment_audit(amendment_id,action,actor,note) values(?,?,?,?)",id,action,actor,note); }
    private static void bad(String message) { throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,message); }
    private static void conflict(String message) { throw new ResponseStatusException(HttpStatus.CONFLICT,message); }
    public record Submission(String ruleNumber,String notificationNumber,LocalDate effectiveDate,String replacementText,
            String sourceUrl,String sourceSha256,int pageStart,int pageEnd) {}
}
