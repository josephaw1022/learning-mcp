package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "EvidenceTag", schema = "evidence")
public class EvidenceTag extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evidence_tag_seq")
    @SequenceGenerator(name = "evidence_tag_seq", sequenceName = "EvidenceTag_SEQ", schema = "evidence", allocationSize = 50)
    public Long id;

    public String tagName;
    public String colorCode;
}
