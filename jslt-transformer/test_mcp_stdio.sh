#!/bin/bash

# Test script for Spring MCP STDIO protocol
# This script tests the basic MCP handshake and tool discovery

echo "Testing Spring MCP STDIO integration..."

# Start the MCP server
MCP_JAR="target/jslt-transformer-1.0-SNAPSHOT-mcp.jar"

# Test MCP initialize request
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}' | \
java -jar "$MCP_JAR" --spring.profiles.active=mcp | head -1

echo ""
echo "MCP Server test completed."
