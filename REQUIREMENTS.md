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
  "data": {...},           // Input JSON data to transform
  "query": "string",       // JSLT transformation query
  "options": {             // Optional transformation options
    "strict": true,        // Enable strict mode (default: false)
    "prettyPrint": true    // Format output JSON (default: false)
  }
}
```

**Response:**
```json
{
  "result": {...},         // Transformed JSON result
  "executionTime": 123,    // Execution time in milliseconds
  "status": "success"      // Status: success, error, warning
}
```

**Error Response:**
```json
{
  "error": {
    "code": "JSLT_SYNTAX_ERROR",
    "message": "Invalid JSLT syntax at line 5",
    "details": "..."
  },
  "status": "error"
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
      "input_data": {
        "type": "object",
        "description": "JSON data to transform"
      },
      "jslt_query": {
        "type": "string",
        "description": "JSLT transformation query"
      },
      "options": {
        "type": "object",
        "properties": {
          "strict_mode": {
            "type": "boolean",
            "description": "Enable strict validation",
            "default": false
          },
          "pretty_print": {
            "type": "boolean",
            "description": "Format output JSON",
            "default": false
          }
        }
      }
    },
    "required": ["input_data", "jslt_query"]
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

### Phase 2: REST API Development (Weeks 3-4)
- [ ] Add Spring Boot framework to existing project
- [ ] Database setup (PostgreSQL) with API key storage
- [ ] Implement REST endpoints using existing transformation logic
- [ ] Implement comprehensive error responses
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Implement rate limiting and security features
- [ ] Add health check and metrics endpoints
- [ ] Performance optimization and testing
- [ ] Integration testing suite

### Phase 3: MCP Integration (Weeks 5-6)
- [ ] Research MCP protocol specification
- [ ] Implement MCP server capabilities
- [ ] Create MCP tool wrapper for JSLT transformation
- [ ] Test integration with Claude Desktop and similar tools
- [ ] Document MCP setup and usage examples

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
- ✅ Successfully transform JSON using JSLT queries
- ✅ Handle malformed input gracefully with clear error messages
- ✅ Provide both REST API and MCP interfaces
- ✅ Support concurrent users without performance degradation

### Technical
- ✅ 99.9% uptime in production
- ✅ < 500ms response time for 95% of requests
- ✅ Support for JSON documents up to 10MB
- ✅ Comprehensive test coverage (>90%)

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
