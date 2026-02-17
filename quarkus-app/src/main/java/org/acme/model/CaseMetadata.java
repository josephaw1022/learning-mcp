package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CaseMetadata", schema = "evidence")
public class CaseMetadata extends PanacheEntityBase {
    @Id
    public String caseId;
    
    public String title;
    public String description;
    public LocalDateTime openedAt;
    public String priority;

    public CaseMetadata() {
        this.openedAt = LocalDateTime.now();
    }
}
