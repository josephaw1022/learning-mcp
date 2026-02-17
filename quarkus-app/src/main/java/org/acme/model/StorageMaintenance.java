package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "StorageMaintenance", schema = "storage")
public class StorageMaintenance extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "storage_maint_seq")
    @SequenceGenerator(name = "storage_maint_seq", sequenceName = "StorageMaintenance_SEQ", schema = "storage", allocationSize = 50)
    public Long id;

    public Long locationId;
    public LocalDateTime checkTime;
    public String performedBy;
    public String results;
}
