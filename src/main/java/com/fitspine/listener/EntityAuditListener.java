package com.fitspine.listener;

import com.fitspine.event.AuditEvent;
import com.fitspine.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EntityAuditListener {

    private final ApplicationEventPublisher eventPublisher;

    @PrePersist
    public void onPrePersist(Object entity) {
        publishAudit(entity, "CREATE");
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        publishAudit(entity, "UPDATE");
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        publishAudit(entity, "DELETE");
    }

    private void publishAudit(Object entity, String action) {
        // Avoid recursion when saving AuditLog itself
        if (entity instanceof AuditLog) return;

        AuditLog log = AuditLog.builder()
                .entityName(entity.getClass().getSimpleName())
                .entityId(extractId(entity))
                .action(action)
                .userEmail(getCurrentUser())
                .details(maskSensitiveData(entity))
                .timestamp(LocalDateTime.now())
                .build();

        // Publish event for async persistence
        eventPublisher.publishEvent(new AuditEvent(this, log));
    }

    private Long extractId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private String maskSensitiveData(Object entity) {
        // Basic placeholder, extend later for real PHI masking
        return entity.getClass().getSimpleName() + " modified";
    }
}
