package org.springboot.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springboot.backend.entity.RuleAmendment;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AmendmentService {

    private final EntityManager entityManager;

    public AmendmentService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<RuleAmendment> activeFor(String ruleNumber, LocalDate asOfDate) {
        return entityManager.createQuery("""
                        select a from RuleAmendment a
                        where lower(a.ruleNumber) = lower(:ruleNumber)
                          and a.status = :status
                          and a.effectiveDate <= :asOfDate
                        order by a.effectiveDate desc
                        """, RuleAmendment.class)
                .setParameter("ruleNumber", ruleNumber)
                .setParameter("status", RuleAmendment.Status.ACTIVE)
                .setParameter("asOfDate", asOfDate)
                .getResultList();
    }

    public boolean needsAugust2026Review(List<String> references, LocalDate asOfDate) {
        return asOfDate.isAfter(LocalDate.of(2026, 8, 27))
                && references.stream().anyMatch(reference -> reference.replace(" ", "").startsWith("2(5)(c)"));
    }
}
