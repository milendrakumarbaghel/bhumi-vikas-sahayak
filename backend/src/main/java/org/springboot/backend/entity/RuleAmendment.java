package org.springboot.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rule_amendment")
public class RuleAmendment {

    public enum Status { DRAFT, REVIEWED, ACTIVE, RETIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_number", nullable = false, length = 64)
    private String ruleNumber;

    @Column(name = "notification_number", nullable = false)
    private String notificationNumber;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "replacement_text", nullable = false, columnDefinition = "text")
    private String replacementText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.DRAFT;

    protected RuleAmendment() {
    }

    public Long getId() { return id; }
    public String getRuleNumber() { return ruleNumber; }
    public String getNotificationNumber() { return notificationNumber; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getReplacementText() { return replacementText; }
    public Status getStatus() { return status; }
}
