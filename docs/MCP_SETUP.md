# JSLT Transformer MCP Server Setup

## Overview

The JSLT Transformer now includes a **Spring AI MCP (Model Context Protocol) server** that exposes JSLT transformation capabilities to AI applications like Claude Desktop. This allows AI models to perform JSON transformations using JSLT queries directly through the MCP protocol.

## Features

- **Spring AI MCP Integration**: Uses Spring AI MCP framework (version 1.0.0-M6) for robust MCP server implementation
- **STDIO Transport**: Optimized for Claude Desktop integration via stdin/stdout communication  
- **Tool-based Architecture**: Exposes JSLT transformation as an MCP tool with proper schema validation
- **File-only Logging**: Prevents console output interference with MCP JSON-RPC protocol
- **Standalone JAR**: Self-contained executable for easy deployment

## MCP Tool: `jslt_transform`

The MCP server exposes a single tool called `jslt_transform` with the following capabilities:

### Parameters
- `inputJson` (required): JSON data to transform as a string
- `jsltQuery` (required): JSLT transformation query/template  

### Response Format
```json
{
  "transformedJson": "...",     // Transformed JSON result (null on error)
  "success": true,              // Transformation success flag
  "processingTimeMs": 125,      // Processing time in milliseconds  
  "errorMessage": null          // Error message (null on success)
}
```

## Quick Start

### 1. Build the MCP Server

```bash
# Build all JARs including MCP server
mvn clean package -DskipTests

# The MCP server JAR will be created as:
# target/jslt-transformer-1.0-SNAPSHOT-mcp.jar
```

### 2. Configure Claude Desktop

Create or update your Claude Desktop MCP configuration file:

**Location**: `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS)

```json
{
  "mcpServers": {
    "jslt-transformer": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/jslt-transformer/target/jslt-transformer-1.0-SNAPSHOT-mcp.jar",
        "--spring.profiles.active=mcp"
      ],
      "env": {
        "JAVA_HOME": "/opt/homebrew/opt/openjdk@17"
      }
    }
  }
}
```

### 3. Test the Integration

1. Restart Claude Desktop after updating the configuration
2. In a new Claude conversation, the JSLT transformation tool should be automatically available
3. Ask Claude to transform JSON data using JSLT queries

## Example Usage in Claude

```
Transform this JSON data:
{"users": [{"name": "John", "age": 30}, {"name": "Jane", "age": 25}]}

Using this JSLT query to extract just the names:
[.users[].name]
```

Claude will automatically use the `jslt_transform` tool and return the transformed result.

## Architecture

### Spring MCP Components

- **`SpringMcpServerApplication`**: Main Spring Boot application for MCP server
- **`JsltMcpService`**: MCP service that exposes JSLT transformation as a Spring AI MCP tool
- **Configuration Files**:
  - `application-mcp.yml`: Spring configuration for MCP profile
  - `logback-mcp.xml`: File-only logging configuration

### Key Dependencies

- **Spring Boot**: 3.2.1
- **Spring AI MCP Server**: 1.0.0-M6 (Spring Boot Starter)
- **JSLT Library**: 0.1.14 (for JSON transformation)

## Migration from Standalone MCP

The previous standalone MCP server (`McpServerMain`) has been **deprecated** in favor of this Spring AI MCP implementation. The Spring implementation provides:

- Better integration with Spring ecosystem
- Improved tool management and validation  
- Enhanced error handling and logging
- More maintainable and testable code

## Troubleshooting

### Common Issues

1. **MCP server not starting**: Check Java version (requires Java 17+) and ensure all dependencies are available
2. **Claude can't find tools**: Verify Claude Desktop configuration path and restart Claude Desktop
3. **JSON-RPC communication issues**: Check that file-only logging is configured (no console output)

### Debug Logging

MCP server logs are written to `logs/mcp-server.log`. Check this file for detailed startup and operation logs.

### Testing MCP Protocol

You can manually test the MCP protocol using:

```bash
# Test basic MCP initialization
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{}}}' | \
java -jar target/jslt-transformer-1.0-SNAPSHOT-mcp.jar --spring.profiles.active=mcp
```

## Production Deployment

For production use:

1. Build the MCP server JAR: `mvn clean package -DskipTests`  
2. Deploy the JAR to your target environment
3. Configure appropriate JVM settings (memory, GC, etc.)
4. Set up monitoring and log rotation for `logs/mcp-server.log`
5. Configure Claude Desktop on client machines to point to the deployed server

## Related Documentation

- [JSLT Language Reference](https://github.com/schibsted/jslt)
- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/mcp.html)
- [Model Context Protocol Specification](https://github.com/anthropics/mcp)
