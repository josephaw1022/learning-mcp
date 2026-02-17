package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "EvidenceItem", schema = "evidence")
public class EvidenceItem extends PanacheEntityBase {
    @Id
    public UUID uuid;

    public String caseId;
    public String filename;
    public String mimeType;
    public Long fileSizeBytes;
    public String sha256Hash;
    public String storageProviderRef;

    @Enumerated(EnumType.STRING)
    public EvidenceStatus status;

    public EvidenceItem() {
        this.uuid = UUID.randomUUID();
    }
}
