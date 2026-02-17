"""Tools for managing evidence items in the Chain-of-Custody system."""

import httpx
from typing import Optional, List, Any
from mcp.types import ToolAnnotations
from core.server import mcp
from core.utils import get_env_var

BASE_URL = get_env_var("QUARKUS_BASE_URL", "http://localhost:8080").rstrip("/") + "/evidence"


@mcp.tool(
    name="evidence_search",
    description="Query the evidence database using case identifiers or lifecycle status.",
    tags=["forensics", "search", "inventory"],
    annotations={"readOnlyHint": True},
)
async def search_evidence(
    case_id: Optional[str] = None, status: Optional[str] = None
) -> str:
    """Search for evidence items by case ID or status.

    Args:
        case_id: Optional Case ID to filter by (e.g. 'CASE-1234')
        status: Optional status filter (INGESTED, ACTIVE, DEPRECATED, PURGED)
    """
    params = {}
    if case_id:
        params["caseId"] = case_id
    if status:
        params["status"] = status

    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/search", params=params)
        response.raise_for_status()
        items = response.json()

        if not items:
            return "No evidence items found matching the criteria."

        output = ["Evidence Items Found:"]
        for item in items[:10]:  # Limit to top 10 for brevity
            output.append(
                f"- UUID: {item['uuid']}, Filename: {item['filename']}, Status: {item['status']}, Case: {item['caseId']}"
            )

        if len(items) > 10:
            output.append(f"... and {len(items) - 10} more.")

        return "\n".join(output)


@mcp.tool(
    name="evidence_chain_history",
    description="Retrieve the complete immutable audit trail for a piece of evidence.",
    tags=["audit", "compliance", "history"],
    annotations={"readOnlyHint": True},
)
async def get_evidence_history(evidence_uuid: str) -> str:
    """Retrieve the full audit trail (Chain of Custody) for a specific evidence item.

    Args:
        evidence_uuid: The unique UUID of the evidence item.
    """
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/{evidence_uuid}/history")
        if response.status_code == 404:
            return f"Evidence item {evidence_uuid} not found."
        response.raise_for_status()
        entries = response.json()

        if not entries:
            return f"No history found for evidence item {evidence_uuid}."

        output = [f"Audit History for {evidence_uuid}:"]
        for entry in entries:
            output.append(
                f"[{entry['timestamp']}] Actor: {entry['actorId']}, Action: {entry['actionType']}, Notes: {entry['notes']}"
            )

        return "\n".join(output)


@mcp.tool(
    name="evidence_custody_transfer",
    description="Record a legal transfer of evidence between two authorized users.",
    tags=["workflow", "legal", "transfer"],
)
async def transfer_evidence(
    evidence_uuid: str, from_user: str, to_user: str, reason: str, actor_id: str
) -> str:
    """Transfer legal custody of an evidence item from one officer/user to another.

    Args:
        evidence_uuid: The UUID of the evidence item.
        from_user: The ID/Badge number of the current custodian.
        to_user: The ID/Badge number of the new custodian.
        reason: The reason for the transfer (e.g. 'Lab Analysis', 'Storage Move').
        actor_id: The ID of the user performing this action.
    """
    payload = {
        "fromUserId": from_user,
        "toUserId": to_user,
        "reason": reason,
        "actorId": actor_id,
    }
    async with httpx.AsyncClient() as client:
        response = await client.patch(
            f"{BASE_URL}/{evidence_uuid}/transfer", json=payload
        )
        if response.status_code == 404:
            return f"Evidence item {evidence_uuid} not found."
        response.raise_for_status()
        return f"Successfully transferred custody of {evidence_uuid} to {to_user}."


@mcp.tool(
    name="evidence_integrity_check",
    description="Verify that the stored file has not been tampered with by recalculating its SHA-256 hash.",
    tags=["security", "integrity", "forensics"],
)
async def verify_evidence(evidence_uuid: str, actor_id: str) -> str:
    """Trigger a manual integrity check (SHA-256 re-hashing) for an evidence item.

    Args:
        evidence_uuid: The UUID of the evidence item.
        actor_id: The ID of the user performing the verification.
    """
    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{BASE_URL}/{evidence_uuid}/verify", params={"actorId": actor_id}
        )
        if response.status_code == 404:
            return f"Evidence item {evidence_uuid} not found."
        elif response.status_code == 409:
            return f"INTEGRITY FAILURE: Evidence item {evidence_uuid} hash mismatch detected! Item has been flagged."
        response.raise_for_status()
        return f"Integrity verified for {evidence_uuid}. Hash matches record."
