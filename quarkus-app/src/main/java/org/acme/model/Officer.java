package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Officer", schema = "users")
public class Officer extends PanacheEntityBase {
    @Id
    public String badgeNumber;
    public String fullName;
    public String departmentName;
    
    @Enumerated(EnumType.STRING)
    public UserRole role;

    public Officer() {}
}
