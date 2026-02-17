package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "AuditEntry", schema = "audit")
public class AuditEntry extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(name = "audit_seq", sequenceName = "AuditEntry_SEQ", schema = "audit", allocationSize = 50)
    public Long id;

    public LocalDateTime timestamp;
    public String actorId;
    public UUID evidenceId;

    @Enumerated(EnumType.STRING)
    public ActionType actionType;

    public String notes;

    public AuditEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public static void log(UUID evidenceId, String actorId, ActionType actionType, String notes) {
        AuditEntry entry = new AuditEntry();
        entry.evidenceId = evidenceId;
        entry.actorId = actorId;
        entry.actionType = actionType;
        entry.notes = notes;
        entry.persist();
    }
}
