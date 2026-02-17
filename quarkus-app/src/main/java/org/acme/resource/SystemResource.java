package org.acme.resource;

import org.acme.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SystemResource {

    @GET
    @Path("/facilities")
    public List<StorageFacility> listFacilities() {
        return StorageFacility.listAll();
    }

    @GET
    @Path("/locations")
    public List<StorageLocation> listLocations(@QueryParam("facilityId") String facilityId) {
        if (facilityId != null) {
            return StorageLocation.list("facilityId", facilityId);
        }
        return StorageLocation.listAll();
    }

    @GET
    @Path("/alerts")
    public List<SystemAlert> listAlerts(@QueryParam("resolved") Boolean resolved) {
        if (resolved != null) {
            return SystemAlert.list("isResolved", resolved);
        }
        return SystemAlert.listAll();
    }

    @GET
    @Path("/officers")
    public List<Officer> listOfficers(@QueryParam("department") String department) {
        if (department != null) {
            return Officer.list("departmentName", department);
        }
        return Officer.listAll();
    }
}
