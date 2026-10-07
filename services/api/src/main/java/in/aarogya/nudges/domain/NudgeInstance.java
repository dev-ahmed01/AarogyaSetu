package in.aarogya.nudges.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "nudge_instances")
public class NudgeInstance {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "nudge_key", nullable = false, length = 220)
    private String nudgeKey;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, length = 1800)
    private String message;

    @Column(name = "action_label", length = 120)
    private String actionLabel;

    @Column(name = "action_href", length = 240)
    private String actionHref;

    @Column(name = "reason_code", nullable = false, length = 120)
    private String reasonCode;

    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_ref", length = 220)
    private String sourceRef;

    @Column(name = "evidence_label", length = 260)
    private String evidenceLabel;

    @Column(name = "evidence_url", length = 600)
    private String evidenceUrl;

    @Column(name = "first_generated_at", nullable = false)
    private Instant firstGeneratedAt;

    @Column(name = "last_evaluated_at", nullable = false)
    private Instant lastEvaluatedAt;

    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected NudgeInstance() {
    }

    public NudgeInstance(
        UserAccount user,
        String nudgeKey,
        NudgeRule rule,
        String title,
        String message,
        String actionLabel,
        String actionHref,
        String reasonCode,
        String sourceType,
        String sourceRef,
        String evidenceLabel,
        String evidenceUrl
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.nudgeKey = nudgeKey;
        this.status = "ACTIVE";
        applyRule(rule);
        replaceContent(
            title,
            message,
            actionLabel,
            actionHref,
            reasonCode,
            sourceType,
            sourceRef,
            evidenceLabel,
            evidenceUrl
        );
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (firstGeneratedAt == null) firstGeneratedAt = now;
        if (lastEvaluatedAt == null) lastEvaluatedAt = now;
    }

    public void refresh(
        NudgeRule rule,
        String title,
        String message,
        String actionLabel,
        String actionHref,
        String reasonCode,
        String sourceType,
        String sourceRef,
        String evidenceLabel,
        String evidenceUrl,
        Instant now
    ) {
        applyRule(rule);
        replaceContent(
            title,
            message,
            actionLabel,
            actionHref,
            reasonCode,
            sourceType,
            sourceRef,
            evidenceLabel,
            evidenceUrl
        );
        lastEvaluatedAt = now;

        if ("SNOOZED".equals(status)
            && snoozedUntil != null
            && !snoozedUntil.isAfter(now)) {
            status = "ACTIVE";
            snoozedUntil = null;
        }

        if ("RESOLVED".equals(status)) {
            reactivate();
            return;
        }

        var stateTime = "ACKNOWLEDGED".equals(status)
            ? acknowledgedAt
            : "DISMISSED".equals(status)
                ? dismissedAt
                : null;

        if (stateTime != null
            && Duration.between(stateTime, now).toHours() >= rule.getCooldownHours()) {
            reactivate();
        }
    }

    public void snooze(Instant until) {
        this.status = "SNOOZED";
        this.snoozedUntil = until;
        this.acknowledgedAt = null;
        this.dismissedAt = null;
        this.resolvedAt = null;
    }

    public void acknowledge(Instant now) {
        this.status = "ACKNOWLEDGED";
        this.acknowledgedAt = now;
        this.snoozedUntil = null;
        this.dismissedAt = null;
        this.resolvedAt = null;
    }

    public void dismiss(Instant now) {
        this.status = "DISMISSED";
        this.dismissedAt = now;
        this.snoozedUntil = null;
        this.acknowledgedAt = null;
        this.resolvedAt = null;
    }

    public void resolve(Instant now) {
        if ("ACTIVE".equals(status) || "SNOOZED".equals(status)) {
            this.status = "RESOLVED";
            this.resolvedAt = now;
            this.snoozedUntil = null;
        }
        this.lastEvaluatedAt = now;
    }

    private void reactivate() {
        this.status = "ACTIVE";
        this.snoozedUntil = null;
        this.acknowledgedAt = null;
        this.dismissedAt = null;
        this.resolvedAt = null;
    }

    private void applyRule(NudgeRule rule) {
        this.ruleCode = rule.getRuleCode();
        this.ruleVersion = rule.getRuleVersion();
        this.category = rule.getCategory();
        this.severity = rule.getSeverity();
    }

    private void replaceContent(
        String title,
        String message,
        String actionLabel,
        String actionHref,
        String reasonCode,
        String sourceType,
        String sourceRef,
        String evidenceLabel,
        String evidenceUrl
    ) {
        this.title = title;
        this.message = message;
        this.actionLabel = actionLabel;
        this.actionHref = actionHref;
        this.reasonCode = reasonCode;
        this.sourceType = sourceType;
        this.sourceRef = sourceRef;
        this.evidenceLabel = evidenceLabel;
        this.evidenceUrl = evidenceUrl;
    }

    public UUID getId() { return id; }
    public String getNudgeKey() { return nudgeKey; }
    public String getRuleCode() { return ruleCode; }
    public int getRuleVersion() { return ruleVersion; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getActionLabel() { return actionLabel; }
    public String getActionHref() { return actionHref; }
    public String getReasonCode() { return reasonCode; }
    public String getSourceType() { return sourceType; }
    public String getSourceRef() { return sourceRef; }
    public String getEvidenceLabel() { return evidenceLabel; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public Instant getFirstGeneratedAt() { return firstGeneratedAt; }
    public Instant getLastEvaluatedAt() { return lastEvaluatedAt; }
    public Instant getSnoozedUntil() { return snoozedUntil; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getDismissedAt() { return dismissedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
