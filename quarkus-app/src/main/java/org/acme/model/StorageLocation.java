package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "StorageLocation", schema = "storage")
public class StorageLocation extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "storage_loc_seq")
    @SequenceGenerator(name = "storage_loc_seq", sequenceName = "StorageLocation_SEQ", schema = "storage", allocationSize = 50)
    public Long id;

    public String label;
    @Enumerated(EnumType.STRING)
    public StorageType type;
    public Integer capacity;
    public String facilityId;
}
