package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Department", schema = "users")
public class Department extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_dept_seq")
    @SequenceGenerator(name = "users_dept_seq", sequenceName = "Department_SEQ", schema = "users", allocationSize = 50)
    public Long id;

    public String name;
    public String location;
    public String contactEmail;
}
