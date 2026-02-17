package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Custodian", schema = "evidence")
public class Custodian extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evidence_custodian_seq")
    @SequenceGenerator(name = "evidence_custodian_seq", sequenceName = "Custodian_SEQ", schema = "evidence", allocationSize = 50)
    public Long id;

    public UUID evidenceId;
    public String userId;
    public LocalDateTime assignedAt;
    public String notes;

    public Custodian() {
        this.assignedAt = LocalDateTime.now();
    }
}
