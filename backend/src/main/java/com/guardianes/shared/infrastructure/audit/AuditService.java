package com.guardianes.shared.infrastructure.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.shared.domain.model.GuardianId;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Audit service for tracking important business events and user actions. Provides comprehensive
 * audit trail for security and compliance purposes.
 */
@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final ObjectMapper objectMapper;

    public AuditService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Records an audit event with structured logging */
    public void recordEvent(AuditEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            auditLogger.info("AUDIT_EVENT: {}", eventJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize audit event", e);
            // Fallback to simple logging
            auditLogger.info(
                    "AUDIT_EVENT: action={}, guardianId={}, timestamp={}",
                    event.getAction(),
                    event.getGuardianId(),
                    event.getTimestamp());
        }
    }

    /** Records step submission events */
    @Async("auditExecutor")
    @EventListener
    public void auditStepSubmission(StepSubmissionEvent event) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .action("STEP_SUBMISSION")
                        .guardianId(event.getGuardianId())
                        .details(
                                Map.of(
                                        "step_count", event.getStepCount(),
                                        "energy_generated", event.getEnergyGenerated(),
                                        "submission_timestamp", event.getSubmissionTimestamp()))
                        .timestamp(Instant.now())
                        .build();

        recordEvent(auditEvent);
    }

    /** Records authentication events */
    @Async("auditExecutor")
    @EventListener
    public void auditAuthentication(AuthenticationEvent event) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .action("AUTHENTICATION")
                        .guardianId(event.getGuardianId())
                        .details(
                                Map.of(
                                        "success", event.isSuccess(),
                                        "ip_address", event.getIpAddress(),
                                        "user_agent", event.getUserAgent()))
                        .timestamp(Instant.now())
                        .build();

        recordEvent(auditEvent);
    }

    /** Records energy transaction events */
    @Async("auditExecutor")
    @EventListener
    public void auditEnergyTransaction(EnergyTransactionEvent event) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .action("ENERGY_TRANSACTION")
                        .guardianId(event.getGuardianId())
                        .details(
                                Map.of(
                                        "transaction_type", event.getTransactionType(),
                                        "amount", event.getAmount(),
                                        "source", event.getSource(),
                                        "balance_after", event.getBalanceAfter()))
                        .timestamp(Instant.now())
                        .build();

        recordEvent(auditEvent);
    }

    /** Records security events (rate limiting, suspicious activity, etc.) */
    @Async("auditExecutor")
    @EventListener
    public void auditSecurityEvent(SecurityEvent event) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .action("SECURITY_EVENT")
                        .guardianId(event.getGuardianId())
                        .details(
                                Map.of(
                                        "event_type", event.getEventType(),
                                        "severity", event.getSeverity(),
                                        "description", event.getDescription(),
                                        "ip_address", event.getIpAddress()))
                        .timestamp(Instant.now())
                        .build();

        recordEvent(auditEvent);
    }

    /** Records system administration events */
    public void auditAdminAction(String action, String adminUser, Map<String, Object> details) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .action("ADMIN_ACTION")
                        .details(
                                Map.of(
                                        "admin_user", adminUser,
                                        "admin_action", action,
                                        "details", details))
                        .timestamp(Instant.now())
                        .build();

        recordEvent(auditEvent);
    }

    /** Structured audit event class */
    public static class AuditEvent {
        private String action;
        private GuardianId guardianId;
        private Map<String, Object> details;
        private Instant timestamp;

        public static AuditEventBuilder builder() {
            return new AuditEventBuilder();
        }

        // Getters
        public String getAction() {
            return action;
        }

        public GuardianId getGuardianId() {
            return guardianId;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        // Builder
        public static class AuditEventBuilder {
            private String action;
            private GuardianId guardianId;
            private Map<String, Object> details;
            private Instant timestamp;

            public AuditEventBuilder action(String action) {
                this.action = action;
                return this;
            }

            public AuditEventBuilder guardianId(GuardianId guardianId) {
                this.guardianId = guardianId;
                return this;
            }

            public AuditEventBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public AuditEventBuilder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public AuditEvent build() {
                AuditEvent event = new AuditEvent();
                event.action = this.action;
                event.guardianId = this.guardianId;
                event.details = this.details;
                event.timestamp = this.timestamp;
                return event;
            }
        }
    }

    // Event classes for different types of audit events
    public static class StepSubmissionEvent {
        private final GuardianId guardianId;
        private final int stepCount;
        private final int energyGenerated;
        private final Instant submissionTimestamp;

        public StepSubmissionEvent(
                GuardianId guardianId,
                int stepCount,
                int energyGenerated,
                Instant submissionTimestamp) {
            this.guardianId = guardianId;
            this.stepCount = stepCount;
            this.energyGenerated = energyGenerated;
            this.submissionTimestamp = submissionTimestamp;
        }

        public GuardianId getGuardianId() {
            return guardianId;
        }

        public int getStepCount() {
            return stepCount;
        }

        public int getEnergyGenerated() {
            return energyGenerated;
        }

        public Instant getSubmissionTimestamp() {
            return submissionTimestamp;
        }
    }

    public static class AuthenticationEvent {
        private final GuardianId guardianId;
        private final boolean success;
        private final String ipAddress;
        private final String userAgent;

        public AuthenticationEvent(
                GuardianId guardianId, boolean success, String ipAddress, String userAgent) {
            this.guardianId = guardianId;
            this.success = success;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }

        public GuardianId getGuardianId() {
            return guardianId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }
    }

    public static class EnergyTransactionEvent {
        private final GuardianId guardianId;
        private final String transactionType;
        private final int amount;
        private final String source;
        private final int balanceAfter;

        public EnergyTransactionEvent(
                GuardianId guardianId,
                String transactionType,
                int amount,
                String source,
                int balanceAfter) {
            this.guardianId = guardianId;
            this.transactionType = transactionType;
            this.amount = amount;
            this.source = source;
            this.balanceAfter = balanceAfter;
        }

        public GuardianId getGuardianId() {
            return guardianId;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public int getAmount() {
            return amount;
        }

        public String getSource() {
            return source;
        }

        public int getBalanceAfter() {
            return balanceAfter;
        }
    }

    public static class SecurityEvent {
        private final GuardianId guardianId;
        private final String eventType;
        private final String severity;
        private final String description;
        private final String ipAddress;

        public SecurityEvent(
                GuardianId guardianId,
                String eventType,
                String severity,
                String description,
                String ipAddress) {
            this.guardianId = guardianId;
            this.eventType = eventType;
            this.severity = severity;
            this.description = description;
            this.ipAddress = ipAddress;
        }

        public GuardianId getGuardianId() {
            return guardianId;
        }

        public String getEventType() {
            return eventType;
        }

        public String getSeverity() {
            return severity;
        }

        public String getDescription() {
            return description;
        }

        public String getIpAddress() {
            return ipAddress;
        }
    }
}
