package org.acme.resource;

import org.acme.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportingResource {

    @GET
    @Path("/case-summary/{caseId}")
    public Map<String, Object> getCaseSummary(@PathParam("caseId") String caseId) {
        CaseMetadata metadata = CaseMetadata.findById(caseId);
        List<EvidenceItem> items = EvidenceItem.list("caseId", caseId);
        
        Map<String, Long> statusCounts = items.stream()
            .collect(Collectors.groupingBy(i -> i.status.toString(), Collectors.counting()));
            
        Map<String, Object> report = new HashMap<>();
        report.put("metadata", metadata);
        report.put("totalItems", items.size());
        report.put("statusDistribution", statusCounts);
        
        // Check for any failed integrity checks in audit logs for these items
        List<UUID> uuids = items.stream().map(i -> i.uuid).collect(Collectors.toList());
        if (!uuids.isEmpty()) {
            long alerts = AuditEntry.count("evidenceId in ?1 and actionType = ?2", uuids, ActionType.INTEGRITY_CHECK_FAIL);
            report.put("securityAlerts", alerts);
        } else {
            report.put("securityAlerts", 0);
        }
        
        return report;
    }

    @GET
    @Path("/integrity-danger-zone")
    public List<EvidenceItem> getIntegrityDangerZone() {
        // Items that have a failed integrity check in their history and aren't PURGED
        List<AuditEntry> failedChecks = AuditEntry.list("actionType", ActionType.INTEGRITY_CHECK_FAIL);
        Set<UUID> dangerousUuids = failedChecks.stream().map(a -> a.evidenceId).collect(Collectors.toSet());
        
        if (dangerousUuids.isEmpty()) return Collections.emptyList();
        
        return EvidenceItem.list("uuid in ?1 and status != ?2", dangerousUuids, EvidenceStatus.PURGED);
    }

    @GET
    @Path("/officer-workload")
    public List<Map<String, Object>> getOfficerWorkload() {
        // This is a bit "heavy" for Active Record, but for sample data it's fine
        List<Officer> officers = Officer.listAll();
        return officers.stream().map(o -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("badgeNumber", o.badgeNumber);
            stat.put("fullName", o.fullName);
            stat.put("itemCount", Custodian.count("userId", o.badgeNumber));
            return stat;
        }).sorted((a, b) -> ((Long)b.get("itemCount")).compareTo((Long)a.get("itemCount")))
          .limit(10)
          .collect(Collectors.toList());
    }
}
