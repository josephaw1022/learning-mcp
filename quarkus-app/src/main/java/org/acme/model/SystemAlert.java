package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SystemAlert", schema = "audit")
public class SystemAlert extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_alert_seq")
    @SequenceGenerator(name = "audit_alert_seq", sequenceName = "SystemAlert_SEQ", schema = "audit", allocationSize = 50)
    public Long id;

    public String severity;
    public String message;
    public LocalDateTime alertTime;
    public Boolean isResolved;
}
