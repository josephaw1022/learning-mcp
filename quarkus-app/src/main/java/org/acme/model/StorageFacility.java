package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "StorageFacility", schema = "storage")
public class StorageFacility extends PanacheEntityBase {
    @Id
    public String facilityId;
    public String name;
    public String address;
    public Boolean isActive;
}
