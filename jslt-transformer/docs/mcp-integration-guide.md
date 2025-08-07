# MCP Integration Guide

## Overview

The JSLT Transformation Service includes Model Context Protocol (MCP) integration, allowing AI models like Claude Desktop to invoke JSLT transformations as tools. This enables natural language interaction with JSON transformation capabilities.

## What is MCP?

Model Context Protocol (MCP) is a standardized protocol that allows AI applications to connect to external services and tools. It uses JSON-RPC over stdio or HTTP transports to enable AI models to discover and invoke external capabilities.

### Key Components

- **Host**: AI application (e.g., Claude Desktop)
- **Client**: MCP client within the AI application
- **Server**: Our JSLT Transformation Service acting as an MCP server

## MCP Server Configuration

The MCP server can be configured via `application.yml`:

```yaml
mcp:
  server:
    enabled: true
    name: "JSLT Transformation Service"
    version: "1.0.0"
    description: "Transform JSON data using JSLT queries"
    stdio:
      enabled: true
    http:
      enabled: false
      port: 8081
```

### Configuration Properties

- `mcp.server.enabled`: Enable/disable MCP server (default: true)
- `mcp.server.name`: Server name displayed to MCP clients
- `mcp.server.version`: Server version
- `mcp.server.description`: Server description
- `mcp.server.stdio.enabled`: Enable stdio transport (default: true)
- `mcp.server.http.enabled`: Enable HTTP transport (default: false)
- `mcp.server.http.port`: HTTP transport port (default: 8081)

## Available Tools

The MCP server exposes one primary tool:

### jslt_transform

Transform JSON data using JSLT (JSON Stylesheet Language Transformations).

**Parameters:**
- `jsonData` (required): JSON data to transform (string or object)
- `jsltQuery` (required): JSLT transformation query
- `prettyPrint` (optional): Pretty-print output JSON (default: true)
- `returnAsString` (optional): Return result as JSON string (default: false)

**Example Usage:**
```json
{
  "name": "jslt_transform",
  "arguments": {
    "jsonData": "{\"name\": \"John\", \"age\": 30}",
    "jsltQuery": ".name",
    "prettyPrint": true
  }
}
```

## Running the MCP Server

### Command Line

Start the application in MCP server mode:

```bash
java -jar jslt-transformer-1.0-SNAPSHOT.jar --mcp
```

Or using system property:

```bash
java -Dmcp.mode=true -jar jslt-transformer-1.0-SNAPSHOT.jar
```

### Maven

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--mcp"
```

## Claude Desktop Integration

To integrate with Claude Desktop, add the following to your Claude Desktop MCP configuration:

### macOS Configuration

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "jslt-transformer": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/jslt-transformer-1.0-SNAPSHOT.jar",
        "--mcp"
      ],
      "env": {
        "MCP_MODE": "true"
      }
    }
  }
}
```

### Windows Configuration

Edit `%APPDATA%\Claude\claude_desktop_config.json` with similar configuration.

## MCP Protocol Implementation

The server implements the following MCP methods:

### initialize

Initializes the MCP session and returns server capabilities.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "initialize",
  "id": 1,
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "claude-desktop",
      "version": "0.7.0"
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {
        "listChanged": false
      }
    },
    "serverInfo": {
      "name": "JSLT Transformation Service",
      "version": "1.0.0"
    }
  }
}
```

### tools/list

Lists available tools.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "id": 2,
  "params": {}
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "jslt_transform",
        "description": "Transform JSON data using JSLT queries",
        "inputSchema": {
          "type": "object",
          "properties": {
            "jsonData": {
              "type": "string",
              "description": "JSON data to transform"
            },
            "jsltQuery": {
              "type": "string",
              "description": "JSLT transformation query"
            }
          },
          "required": ["jsonData", "jsltQuery"]
        }
      }
    ]
  }
}
```

### tools/call

Executes a tool with provided arguments.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "id": 3,
  "params": {
    "name": "jslt_transform",
    "arguments": {
      "jsonData": "{\"users\": [{\"name\": \"John\", \"age\": 30}]}",
      "jsltQuery": ".users[0].name"
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\n  \"requestId\": \"...\",\n  \"timestamp\": \"...\",\n  \"success\": true,\n  \"result\": \"John\"\n}"
      }
    ]
  }
}
```

## Error Handling

The MCP server follows JSON-RPC error conventions:

- `-32700`: Parse error
- `-32600`: Invalid request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error

Example error response:
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error": {
    "code": -32602,
    "message": "Invalid params",
    "data": "Missing required parameter: jsltQuery"
  }
}
```

## Security Considerations

- The MCP server runs locally and communicates via stdio by default
- HTTP transport is disabled by default for security
- All JSLT transformations are sandboxed within the Java JSLT library
- Input validation is performed on all parameters
- Detailed error logging is available for debugging

## Troubleshooting

### Common Issues

1. **Server not starting**: Check that `mcp.server.enabled=true` in configuration
2. **Claude Desktop not connecting**: Verify the path to JAR file in configuration
3. **Tool not available**: Ensure server initialization completed successfully
4. **Transformation errors**: Check JSLT query syntax and JSON data format

### Logging

Enable debug logging for MCP components:

```yaml
logging:
  level:
    com.mxai.jslt.mcp: DEBUG
```

### Testing

Run manual tests to validate MCP functionality:

```bash
mvn test -Dtest="McpServerManualTest"
```

## Architecture

The MCP integration consists of:

- `McpServerConfiguration`: Configuration properties and binding
- `McpServerService`: Core MCP protocol implementation and tool execution
- `McpServerStarter`: Application runner for MCP mode startup
- Manual and unit tests for validation

The implementation uses:
- Jackson for JSON processing
- Spring Boot for configuration and dependency injection
- Custom JSON-RPC protocol handling
- Integration with existing JSLT transformation service
