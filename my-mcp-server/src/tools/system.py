"""Tools for managing system resources like storage and personnel."""

import httpx
from typing import Optional
from mcp.types import ToolAnnotations
from core.server import mcp
from core.utils import get_env_var

BASE_URL = get_env_var("QUARKUS_BASE_URL", "http://localhost:8080").rstrip("/") + "/system"


@mcp.tool(
    name="system_list_facilities",
    description="Get an overview of all physical and virtual storage facilities in the network.",
    tags=["infrastructure", "facilities", "inventory"],
    annotations={"readOnlyHint": True},
)
async def list_storage_facilities() -> str:
    """List all registered storage facilities."""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/facilities")
        response.raise_for_status()
        facilities = response.json()

        output = ["Storage Facilities:"]
        for f in facilities:
            status = "Active" if f["isActive"] else "Inactive"
            output.append(
                f"- {f['name']} (ID: {f['facilityId']}) - {status} - {f['address']}"
            )
        return "\n".join(output)


@mcp.tool(
    name="system_list_locations",
    description="List specific storage bins, lockers, or buckets within a facility.",
    tags=["logistics", "inventory", "storage"],
    annotations={"readOnlyHint": True},
)
async def list_storage_locations(facility_id: Optional[str] = None) -> str:
    """List storage locations, optionally filtered by facility ID.

    Args:
        facility_id: Optional ID of the facility to filter locations by.
    """
    params = {}
    if facility_id:
        params["facilityId"] = facility_id

    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/locations", params=params)
        response.raise_for_status()
        locations = response.json()

        output = [f"Storage Locations (Facility: {facility_id or 'All'}):"]
        for loc in locations[:20]:
            output.append(
                f"- {loc['label']} (ID: {loc['id']}) Type: {loc['type']}, Capacity: {loc['capacity']}"
            )

        if len(locations) > 20:
            output.append(f"... and {len(locations) - 20} more.")
        return "\n".join(output)


@mcp.tool(
    name="system_alerts_monitor",
    description="Check for critical system alerts, hardware failures, or integrity warnings.",
    tags=["monitoring", "security", "ops"],
    annotations={"readOnlyHint": True},
)
async def get_system_alerts(unresolved_only: bool = True) -> str:
    """Retrieve system alerts from the audit logs.

    Args:
        unresolved_only: If true, only return alerts that haven't been resolved yet.
    """
    params = {"resolved": not unresolved_only} if unresolved_only else {}
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/alerts", params=params)
        response.raise_for_status()
        alerts = response.json()

        if not alerts:
            return "No alerts found."

        output = ["System Alerts:"]
        for a in alerts[:15]:
            output.append(
                f"[{a['alertTime']}] {a['severity']}: {a['message']} (Resolved: {a['isResolved']})"
            )
        return "\n".join(output)


@mcp.tool(
    name="system_personnel_list",
    description="Lookup authorized personnel, officers, and technicians by department.",
    tags=["hr", "directory", "security"],
    annotations={"readOnlyHint": True},
)
async def list_officers(department: Optional[str] = None) -> str:
    """List officers/personnel, optionally filtered by department.

    Args:
        department: Optional department name to filter by.
    """
    params = {}
    if department:
        params["department"] = department

    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/officers", params=params)
        response.raise_for_status()
        officers = response.json()

        output = [f"Officers (Department: {department or 'All'}):"]
        for o in officers[:20]:
            output.append(
                f"- {o['fullName']} (Badge: {o['badgeNumber']}) Role: {o['role']}, Dept: {o['departmentName']}"
            )

        if len(officers) > 20:
            output.append(f"... and {len(officers) - 20} more.")
        return "\n".join(output)
