package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "EvidenceNote", schema = "evidence")
public class EvidenceNote extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evidence_note_seq")
    @SequenceGenerator(name = "evidence_note_seq", sequenceName = "EvidenceNote_SEQ", schema = "evidence", allocationSize = 50)
    public Long id;

    public UUID evidenceId;
    public String authorId;
    public String noteText;
}
