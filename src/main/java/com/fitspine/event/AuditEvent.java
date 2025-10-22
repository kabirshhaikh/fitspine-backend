package com.fitspine.event;

import com.fitspine.model.AuditLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {
    private final AuditLog auditLog;

    public AuditEvent(Object source, AuditLog auditLog) {
        super(source);
        this.auditLog = auditLog;
    }
}
