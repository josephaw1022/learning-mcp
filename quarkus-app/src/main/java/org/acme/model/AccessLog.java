package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AccessLog", schema = "audit")
public class AccessLog extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_access_seq")
    @SequenceGenerator(name = "audit_access_seq", sequenceName = "AccessLog_SEQ", schema = "audit", allocationSize = 50)
    public Long id;

    public String userId;
    public String action;
    public LocalDateTime timestamp;
    public Boolean success;
}
