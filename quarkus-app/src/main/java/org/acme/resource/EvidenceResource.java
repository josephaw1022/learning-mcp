package org.acme.resource;

import org.acme.model.*;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.PartType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import jakarta.ws.rs.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Path("/evidence")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvidenceResource {

    private static final String STORAGE_DIR = "storage";

    public EvidenceResource() {
        new File(STORAGE_DIR).mkdirs();
    }

    public static class FileUpload {
        @RestForm
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public File file;

        @RestForm
        public String caseId;

        @RestForm
        public String actorId;
    }

    @POST
    @Path("/ingest")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response ingest(@MultipartForm FileUpload upload) throws Exception {
        String hash = calculateHash(upload.file);
        
        EvidenceItem item = new EvidenceItem();
        item.caseId = upload.caseId;
        item.filename = upload.file.getName();
        item.fileSizeBytes = upload.file.length();
        item.sha256Hash = hash;
        item.status = EvidenceStatus.INGESTED;
        
        java.nio.file.Path target = java.nio.file.Path.of(STORAGE_DIR, item.uuid.toString());
        Files.copy(upload.file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        item.storageProviderRef = target.toString();
        
        item.persist();
        
        AuditEntry.log(item.uuid, upload.actorId, ActionType.UPLOAD, "Initial ingestion");
        
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @GET
    @Path("/{id}/download")
    @Transactional
    public Response download(@PathParam("id") UUID id, @QueryParam("actorId") String actorId) throws Exception {
        EvidenceItem item = EvidenceItem.findById(id);
        if (item == null) return Response.status(Response.Status.NOT_FOUND).build();

        File file = new File(item.storageProviderRef);
        String currentHash = calculateHash(file);

        if (!currentHash.equals(item.sha256Hash)) {
            item.status = EvidenceStatus.DEPRECATED;
            AuditEntry.log(item.uuid, "SYSTEM", ActionType.INTEGRITY_CHECK_FAIL, "Tamper Alert: Hash mismatch during download");
            return Response.status(Response.Status.CONFLICT).entity("Tamper Alert: File integrity compromised").build();
        }

        AuditEntry.log(item.uuid, actorId, ActionType.DOWNLOAD_BLOB, "File downloaded after integrity check");
        return Response.ok(file).header("Content-Disposition", "attachment; filename=\"" + item.filename + "\"").build();
    }

    @PATCH
    @Path("/{id}/transfer")
    @Transactional
    public Response transfer(@PathParam("id") UUID id, TransferRequest request) {
        EvidenceItem item = EvidenceItem.findById(id);
        if (item == null) return Response.status(Response.Status.NOT_FOUND).build();

        Custodian custodian = new Custodian();
        custodian.evidenceId = id;
        custodian.userId = request.toUserId;
        custodian.notes = request.reason;
        custodian.persist();

        AuditEntry.log(id, request.actorId, ActionType.TRANSFER_CUSTODY, "Transfer from " + request.fromUserId + " to " + request.toUserId + ": " + request.reason);

        return Response.ok(item).build();
    }

    @GET
    @Path("/{id}/history")
    public List<AuditEntry> history(@PathParam("id") UUID id) {
        return AuditEntry.list("evidenceId", id);
    }

    @GET
    @Path("/search")
    public List<EvidenceItem> search(@QueryParam("caseId") String caseId, @QueryParam("status") EvidenceStatus status) {
        if (caseId != null && status != null) {
            return EvidenceItem.list("caseId = ?1 and status = ?2", caseId, status);
        } else if (caseId != null) {
            return EvidenceItem.list("caseId", caseId);
        } else if (status != null) {
            return EvidenceItem.list("status", status);
        }
        return EvidenceItem.listAll();
    }

    @POST
    @Path("/{id}/verify")
    @Transactional
    public Response verify(@PathParam("id") UUID id, @QueryParam("actorId") String actorId) throws Exception {
        EvidenceItem item = EvidenceItem.findById(id);
        if (item == null) return Response.status(Response.Status.NOT_FOUND).build();

        File file = new File(item.storageProviderRef);
        String currentHash = calculateHash(file);

        if (currentHash.equals(item.sha256Hash)) {
            AuditEntry.log(item.uuid, actorId, ActionType.INTEGRITY_CHECK_PASS, "Manual integrity check passed");
            return Response.ok("Integrity verified").build();
        } else {
            item.status = EvidenceStatus.DEPRECATED;
            AuditEntry.log(item.uuid, actorId, ActionType.INTEGRITY_CHECK_FAIL, "Manual integrity check failed: Hash mismatch");
            return Response.status(Response.Status.CONFLICT).entity("Integrity check failed").build();
        }
    }

    private String calculateHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static class TransferRequest {
        public String fromUserId;
        public String toUserId;
        public String reason;
        public String actorId;
    }
}
