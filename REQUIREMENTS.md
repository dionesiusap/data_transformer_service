# JSLT Transformation Service - Requirements & Roadmap

## Project Overview

The JSLT Transformation Service is a Java-based REST API that enables users to transform JSON data using JSLT (JSON Structured Language Transform) queries. The service provides both direct REST API access and an MCP (Model Context Protocol) interface for AI model integration.

## Core Requirements

### 1. Technology Stack
- **Language**: Java (minimum version 11)
- **JSLT Library**: [Schibsted JSLT library](https://github.com/schibsted/jslt)
- **Framework**: Spring Boot (for REST API)
- **Build Tool**: Maven or Gradle
- **Database**: PostgreSQL or H2 (for logging and API key storage)
- **Containerization**: Docker
- **Documentation**: OpenAPI/Swagger for API documentation

### 2. Interface Priority
1. **CLI Interface** (First priority)
2. **REST API** (Second priority)
3. **MCP Service** (Third priority)
4. **Web/Desktop UI** (Future enhancement)

### 3. CLI Interface Requirements

#### 3.1 Command Structure
```bash
jslt-transform --input <input.json> --query <query.jslt> [--output <output.json>] [--pretty]
```

#### 3.2 CLI Features
- Read input JSON from file
- Read JSLT query from file
- Output result to stdout or specified file
- Pretty-print option for formatted output
- Verbose mode for debugging
- Exit codes for success/failure

### 4. REST API Requirements

#### 4.1 Core Endpoints
- `POST /transform` - Execute JSLT transformation
- `GET /health` - Health check endpoint
- `GET /api-docs` - API documentation

#### 4.2 Transform Endpoint Specification
**Request:**
```json
{
  "jsonData": {...} | "string",  // Input JSON data (object or JSON string)
  "jsltQuery": "string",         // JSLT transformation query
  "prettyPrint": false,           // Format output JSON (default: false)
  "returnAsString": false         // Return result as string vs object (default: false)
}
```

**Key Features:**
- **Flexible Input**: `jsonData` accepts both JSON objects and JSON strings
- **Flexible Output**: `returnAsString=false` returns JSON object, `returnAsString=true` returns formatted JSON string
- **Consistent Responses**: All responses use HTTP 200 with structured success/error format
- **Request Tracking**: Every request gets a unique `requestId` for debugging and logging
- **Input Validation**: Maximum 100MB JSON input size with clear error messages

**Success Response:**
```json
{
  "success": true,
  "result": {...},                    // Transformed JSON result (object or string based on returnAsString)
  "errorMessage": null,               // Always null on success
  "processingTimeMs": 123,            // Processing time in milliseconds
  "requestId": "uuid-string",        // Unique request identifier for tracking
  "timestamp": "2025-08-06T16:01:43.554792Z"  // ISO 8601 timestamp
}
```

**Error Response:**
```json
{
  "success": false,
  "result": null,                     // Always null on error
  "errorMessage": "JSLT transformation failed: Parse error...",  // Detailed error message
  "processingTimeMs": null,           // Null when transformation fails
  "requestId": "uuid-string",        // Unique request identifier for tracking
  "timestamp": "2025-08-06T16:01:55.478963Z"  // ISO 8601 timestamp
}
```

#### 4.3 API Features
- **Authentication**: API key-based (X-API-Key header)
- Input validation for JSON and JSLT syntax
- Comprehensive error handling with detailed error messages
- Request/response logging to database
- CORS support for web clients
- Content-Type validation (application/json)
- Support for large payloads (up to single-digit GB)

### 5. MCP Service Requirements

#### 5.1 MCP Architecture Overview
The MCP integration follows the standard Model Context Protocol architecture:
- **Host**: AI applications (Claude Desktop, etc.)
- **Client**: MCP client within the host application
- **Server**: Our JSLT transformation service

Communication uses JSON-RPC protocol over stdio or HTTP transport.

#### 5.2 MCP Tool Definition
```json
{
  "name": "jslt_transform",
  "description": "Transform JSON data using JSLT (JSON Structured Language Transform) queries",
  "inputSchema": {
    "type": "object",
    "properties": {
      "jsonData": {
        "type": ["object", "string"],
        "description": "JSON data to transform (object or JSON string)"
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

#### 5.3 MCP Protocol Implementation
**Required MCP Methods:**
- `tools/list`: Return available tools (jslt_transform)
- `tools/call`: Execute JSLT transformation
- `initialize`: Handle MCP server initialization
- `notifications/initialized`: Confirm initialization complete

**Transport Options:**
- **stdio**: Primary transport for Claude Desktop integration
- **HTTP**: Alternative transport for web-based clients

#### 5.4 MCP Integration Features
- JSON-RPC 2.0 protocol compliance
- User approval workflow (handled by MCP client)
- Structured error responses with MCP error codes
- Support for large JSON transformations
- Logging of all MCP tool invocations

#### 5.5 Claude Desktop Configuration
Users will configure the MCP server in Claude Desktop's config file:
```json
{
  "mcpServers": {
    "jslt-transformer": {
      "command": "java",
      "args": ["-jar", "/path/to/jslt-transformer.jar", "--mcp"]
    }
  }
}
```

### 6. Non-Functional Requirements

#### 6.1 Performance
- Support for large JSON documents (hundreds of MBs to low single-digit GBs)
- Memory-efficient streaming processing for large files
- Low concurrent user load (no specific SLA requirements)
- Configurable timeout for long-running transformations

#### 6.2 Security
- API key authentication (no user management initially)
- Input sanitization to prevent injection attacks
- Request size limits (configurable, up to GB scale)
- HTTPS support in production

#### 6.3 Data Storage
- Database storage for API keys
- Comprehensive logging of all transformations and errors
- Error details stored for debugging purposes

#### 6.4 Reliability
- Graceful error handling for malformed JSON/JSLT
- Circuit breaker pattern for external dependencies
- Health checks and monitoring endpoints
- Structured logging with correlation IDs

#### 6.5 Deployment
- On-premises deployment
- Docker containerization
- Database integration (PostgreSQL recommended)

#### 6.6 Scalability
- Stateless service design
- Docker containerization
- Kubernetes deployment support
- Horizontal scaling capabilities

## Technical Architecture

### 5.1 High-Level Components
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST Client   │    │   MCP Client     │    │  Web Interface  │
└─────────┬───────┘    └─────────┬────────┘    └─────────┬───────┘
          │                      │                       │
          └──────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    API Gateway Layer    │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   JSLT Service Core     │
                    │  - Validation          │
                    │  - Transformation      │
                    │  - Error Handling      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   JSLT Engine          │
                    │  (Schibsted Library)    │
                    └─────────────────────────┘
```

### 5.2 Service Layers
- **Controller Layer**: REST endpoints and request handling
- **Service Layer**: Business logic and JSLT processing
- **Validation Layer**: Input validation and sanitization
- **MCP Layer**: MCP protocol implementation
- **Configuration Layer**: Application configuration management

## Development Roadmap

### Phase 1: CLI Interface (Weeks 1-2) ✅ COMPLETED
- [x] Set up Java project structure with Maven/Gradle
- [x] Integrate JSLT library and validate capabilities
- [x] Implement CLI argument parsing and file I/O
- [x] Core JSLT transformation logic
- [x] Error handling and user-friendly messages
- [x] Unit tests for CLI functionality
- [x] Package as executable JAR

### Phase 2: REST API Development (Weeks 3-4) ✅ COMPLETED
- [x] Spring Boot REST API implementation
- [x] Transform endpoint with flexible input/output formats
- [x] Consistent response structure with error handling
- [x] Request validation and size limits
- [x] Health check and version endpoints
- [x] OpenAPI documentation
- [x] Unit tests and manual validation
- [ ] Database setup (PostgreSQL) with API key storage - **Deferred to Phase 4**
- [ ] Rate limiting and security features - **Deferred to Phase 4**
- [ ] Performance optimization and load testing - **Deferred to Phase 4**

### Phase 3: MCP Integration (Weeks 5-6)
- [x] Research MCP protocol specification and Java SDK options
- [x] Document MCP research findings and implementation strategy
- [ ] Add Spring AI MCP dependencies and initial server configuration
- [ ] Implement JSLT transformation tool with MCP annotations
- [ ] Configure stdio transport for Claude Desktop integration
- [ ] Configure HTTP/SSE transport for web-based AI clients
- [ ] Test tool discovery and execution with MCP Inspector
- [ ] Validate Claude Desktop integration and user approval workflow
- [ ] Create comprehensive MCP setup and usage documentation

### Phase 4: Production Readiness (Weeks 7-8)
- [ ] Docker containerization
- [ ] Kubernetes deployment manifests
- [ ] Production configuration management
- [ ] Monitoring and alerting setup
- [ ] Load testing and performance tuning
- [ ] Security audit and hardening

### Phase 5: Advanced Features (Future)
- [ ] Web-based testing interface
- [ ] JSLT query validation and syntax highlighting
- [ ] Template library for common transformations (e.g., Snowflake to Mendix)
- [ ] Batch transformation support
- [ ] Desktop UI application
- [ ] Metrics dashboard and analytics

## Success Criteria

### Functional
- ✅ **Successfully transform JSON using JSLT queries** - CLI and REST API implemented
- ✅ **Handle malformed input gracefully with clear error messages** - Comprehensive error handling
- ✅ **Flexible input/output formats** - Support JSON objects and strings with configurable output
- ✅ **Consistent API responses** - Structured responses with explicit null fields
- ✅ **Request tracking and logging** - Unique request IDs and timestamps
- 🔄 **Provide both REST API and MCP interfaces** - REST API complete, MCP in progress
- 🔄 **Support concurrent users without performance degradation** - To be validated in Phase 4

### Technical
- ✅ **CLI Interface** - Complete with comprehensive testing
- ✅ **REST API** - Complete with flexible input/output and error handling
- ✅ **Input validation** - Support for JSON documents up to 100MB
- ✅ **Comprehensive test coverage** - Unit tests for all components
- ✅ **OpenAPI documentation** - Complete API specification
- 🔄 **99.9% uptime in production** - To be validated in production deployment
- 🔄 **< 500ms response time for 95% of requests** - To be validated with load testing

### Operational
- ✅ Easy deployment and scaling
- ✅ Comprehensive monitoring and alerting
- ✅ Clear documentation and examples
- ✅ Secure by default configuration

## Dependencies and Assumptions

### External Dependencies
- Schibsted JSLT library availability and stability
- MCP protocol specification and tooling
- Container orchestration platform (Kubernetes/Docker)

### Assumptions
- Users have basic understanding of JSON and JSLT syntax
- Network connectivity for MCP clients
- Adequate infrastructure for expected load

## Risk Assessment

### Technical Risks
- **JSLT Library Limitations**: Mitigation through thorough testing and fallback options
- **Performance with Large Documents**: Mitigation through streaming and chunking
- **MCP Protocol Changes**: Mitigation through versioning and backward compatibility

### Operational Risks
- **Security Vulnerabilities**: Mitigation through regular security audits
- **Scalability Issues**: Mitigation through load testing and monitoring
- **Third-party Dependencies**: Mitigation through dependency monitoring and updates

---

*This document is a living specification and will be updated as requirements evolve and new insights are gained during development.*
