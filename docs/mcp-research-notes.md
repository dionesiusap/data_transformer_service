# MCP Integration Research Notes

**Date:** 2025-08-07  
**Phase:** Phase 3 - MCP Integration Planning  
**Branch:** feature/mcp-integration  

## 🎯 **Research Objective**

Research MCP (Model Context Protocol) Java SDK options and implementation approaches for integrating our JSLT Transformation Service with AI models like Claude Desktop.

---

## 🔍 **Key Findings**

### **1. MCP Architecture Overview**

**Core Components:**
- **Host:** AI applications (Claude Desktop, etc.)
- **Client:** MCP client within the host application  
- **Server:** Our JSLT service (what we need to build)

**Protocol:** JSON-RPC based with standardized message formats

**Transport Options:**
- **stdio:** Process-based communication (preferred for Claude Desktop)
- **HTTP/SSE:** Server-sent events for web-based integration
- **Custom:** Extensible transport layer

### **2. MCP Java SDK Options**

#### **Option A: Official MCP Java SDK**
- **Repository:** https://github.com/modelcontextprotocol/java-sdk
- **Status:** Official SDK maintained by MCP team
- **Architecture:** 3-layer design (Client/Server, Session, Transport)
- **Features:**
  - Full protocol compliance with type safety
  - Synchronous and asynchronous operations
  - Multiple transport implementations
  - Tool discovery and execution
  - Resource management
  - Prompt system interactions

#### **Option B: Spring AI MCP Integration**
- **Repository:** Part of Spring AI project
- **Status:** Official Spring Boot integration
- **Features:**
  - Spring Boot starters for client and server
  - Auto-configuration support
  - Reactive and servlet-based transports
  - Integration with existing Spring Boot apps
  - Bootstrap via Spring Initializer

### **3. MCP Server Implementation Requirements**

**Core Capabilities:**
- **Tool Discovery:** `tools/list` endpoint
- **Tool Execution:** `tools/call` endpoint  
- **Capability Negotiation:** Protocol version and feature support
- **Error Handling:** Structured error responses
- **Logging:** Server-side logging and notifications

**Transport Implementations:**
- **Stdio Transport:** For Claude Desktop integration
- **HTTP/SSE Transport:** For web-based clients
- **WebFlux/WebMVC:** Reactive and servlet-based options

### **4. JSLT Tool Schema Design**

**Tool Definition:**
```json
{
  "name": "jslt_transform",
  "description": "Transform JSON data using JSLT queries",
  "inputSchema": {
    "type": "object",
    "properties": {
      "jsonData": {
        "type": ["object", "string"],
        "description": "JSON data to transform (object or string)"
      },
      "jsltQuery": {
        "type": "string", 
        "description": "JSLT transformation query"
      },
      "prettyPrint": {
        "type": "boolean",
        "description": "Format output with pretty printing",
        "default": false
      },
      "returnAsString": {
        "type": "boolean",
        "description": "Return result as JSON string instead of object",
        "default": false
      }
    },
    "required": ["jsonData", "jsltQuery"]
  }
}
```

**Tool Response:**
```json
{
  "content": [
    {
      "type": "text",
      "text": "Transformation completed successfully"
    }
  ],
  "result": {
    "success": true,
    "data": "transformed_json_result",
    "processingTimeMs": 15,
    "requestId": "uuid"
  }
}
```

---

## 🏗️ **Implementation Strategy**

### **Recommended Approach: Spring AI MCP Integration**

**Rationale:**
- ✅ **Seamless Integration:** Works with our existing Spring Boot application
- ✅ **Auto-Configuration:** Minimal setup required
- ✅ **Multiple Transports:** Supports both stdio and HTTP
- ✅ **Official Support:** Maintained by Spring AI team
- ✅ **Type Safety:** Full Java type support with validation
- ✅ **Reactive Support:** Compatible with our async patterns

### **Implementation Plan**

#### **Phase 3.1: Dependencies and Setup**
1. Add Spring AI MCP server starter dependency
2. Configure MCP server properties
3. Create MCP server configuration class

#### **Phase 3.2: Tool Implementation**
1. Create `JsltTransformTool` class implementing MCP tool interface
2. Define tool schema and input validation
3. Integrate with existing `JsltTransformationService`
4. Implement error handling and logging

#### **Phase 3.3: Transport Configuration**
1. Configure stdio transport for Claude Desktop
2. Configure HTTP/SSE transport for web clients
3. Test transport connectivity

#### **Phase 3.4: Integration Testing**
1. Test with MCP Inspector tool
2. Configure Claude Desktop integration
3. Validate tool discovery and execution
4. Test error scenarios and edge cases

#### **Phase 3.5: Documentation**
1. Create MCP server setup guide
2. Document Claude Desktop configuration
3. Provide usage examples and troubleshooting

---

## 📦 **Dependencies Required**

```xml
<!-- Spring AI MCP Server Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-spring-boot-starter</artifactId>
    <version>${spring-ai.version}</version>
</dependency>

<!-- Additional MCP Core (if needed) -->
<dependency>
    <groupId>io.github.modelcontextprotocol</groupId>
    <artifactId>mcp-server</artifactId>
    <version>${mcp-java-sdk.version}</version>
</dependency>
```

---

## 🔧 **Technical Considerations**

### **Integration Points**
- **Reuse Logic:** Leverage existing `JsltTransformationService`
- **Error Handling:** Consistent with REST API approach
- **Validation:** Use same input validation patterns
- **Logging:** Integrate with existing logback configuration

### **Security**
- **User Approval:** Built-in MCP user consent mechanisms
- **Input Validation:** JSON schema validation for tool inputs
- **Error Sanitization:** Avoid exposing internal system details

### **Performance**
- **Async Processing:** Support for large JSON transformations
- **Resource Management:** Proper cleanup of transformation resources
- **Concurrent Clients:** Handle multiple AI model connections

---

## 🎯 **Success Criteria**

1. **✅ Tool Discovery:** AI models can discover JSLT transformation tool
2. **✅ Tool Execution:** Successful JSON transformations via MCP protocol
3. **✅ Claude Desktop:** Working integration with Claude Desktop app
4. **✅ Error Handling:** Graceful error responses with helpful messages
5. **✅ Documentation:** Complete setup and usage documentation

---

## 🔗 **References**

- [MCP Official Documentation](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Spring AI MCP Integration](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [Claude Desktop MCP Setup](https://modelcontextprotocol.io/quickstart/server)
- [MCP Inspector Tool](https://modelcontextprotocol.io/docs/tools/inspector)

---

## ✅ **Next Steps**

1. **Add Spring AI MCP dependencies** to `pom.xml`
2. **Create MCP server configuration** class
3. **Implement JSLT transformation tool** with proper schema
4. **Configure stdio transport** for Claude Desktop
5. **Test integration** with MCP Inspector and Claude Desktop

---

*Research completed on 2025-08-07 for Phase 3 MCP Integration*
