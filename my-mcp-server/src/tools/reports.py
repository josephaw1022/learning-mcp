"""Tools for high-level business intelligence and reporting.
"""

import httpx
import os
from typing import Optional
from mcp.types import ToolAnnotations
from core.server import mcp

BASE_URL = os.getenv("QUARKUS_BASE_URL", "http://localhost:8080").rstrip("/") + "/reports"

@mcp.tool(
    name="get_case_health_summary",
    description="Provides a high-level summary of a case's evidence status, including security alerts.",
    tags=["reporting", "management", "security"]
)
async def get_case_health_summary(case_id: str) -> str:
    """Analyze all evidence for a given case and report on its overall status.

    Args:
        case_id: The unique Case ID (e.g. 'CASE-1234')
    """
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/case-summary/{case_id}")
        if response.status_code == 404:
            return f"Case {case_id} not found."
        response.raise_for_status()
        data = response.json()
        
        meta = data.get('metadata') or {}
        output = [
            f"Case Health Report: {case_id}",
            f"Title: {meta.get('title', 'N/A')}",
            f"Priority: {meta.get('priority', 'N/A')}",
            f"Total Items: {data['totalItems']}",
            f"Integrity Alerts: {data['securityAlerts']}",
            "Status Distribution:"
        ]
        
        for status, count in data['statusDistribution'].items():
            output.append(f"  - {status}: {count}")
            
        if data['securityAlerts'] > 0:
            output.append("\nWARNING: This case has items with failed integrity checks!")
            
        return "\n".join(output)

@mcp.tool(
    name="find_compromised_evidence",
    description="Scans the system for evidence items that have failed integrity checks but haven't been purged.",
    tags=["security", "audit", "forensics"]
)
async def find_compromised_evidence() -> str:
    """List all evidence items flagged with integrity failures in their history."""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/integrity-danger-zone")
        response.raise_for_status()
        items = response.json()
        
        if not items:
            return "Clear Skies: No compromised evidence items detected in the active inventory."
            
        output = ["COMPROMISED EVIDENCE DETECTED:"]
        for item in items:
            output.append(f"- UUID: {item['uuid']}, File: {item['filename']}, Case: {item['caseId']}, Current Status: {item['status']}")
            
        output.append("\nRecommended Action: Re-verify these items immediately or move to DEPRECATED status.")
        return "\n".join(output)

@mcp.tool(
    name="get_officer_workload_analysis",
    description="Analyzes the current custody distribution to identify officers with high evidence loads.",
    tags=["management", "hr", "operations"]
)
async def get_officer_workload_analysis() -> str:
    """Identify the top 10 officers by current evidence custody count."""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/officer-workload")
        response.raise_for_status()
        stats = response.json()
        
        if not stats:
            return "No custody data available."
            
        output = ["Officer Workload Analysis (Top 10):"]
        for s in stats:
            output.append(f"- {s['fullName']} (Badge: {s['badgeNumber']}): {s['itemCount']} active items")
            
        return "\n".join(output)
