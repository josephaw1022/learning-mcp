# Learning MCP

This repository contains an example of a Model Context Protocol (MCP) server integration with a Quarkus backend.

## Getting Started

Follow these steps to get the environment up and running:

### 1. Start the Database
Use the Taskfile to start the PostgreSQL database container.
```bash
task start
```

### 2. Run the Quarkus REST API
Navigate to the `quarkus-app` directory and start the application in dev mode. This will automatically set up the database schema and expose the REST endpoints.
```bash
cd quarkus-app
./mvnw quarkus:dev
```

### 3. Run the Sample Data Script
In a new terminal, navigate to the `sample-data` directory, install dependencies, and run the script to populate the database.
```bash
cd sample-data
npm install
node main.js
```

### 4. Run the Fast MCP Server
Navigate to the `my-mcp-server` directory and start the MCP server.
```bash
cd my-mcp-server
# Using uv (recommended)
uv run src/main.py
```

### 5. Connect your Model
To use this server with Claude Desktop, add the following to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-mcp-server": {
      "command": "uv",
      "args": [
        "--directory",
        "./my-mcp-server",
        "run",
        "src/main.py"
      ]
    }
  }
}
```
