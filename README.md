# JSLT Transformation Service

A powerful, enterprise-ready JSON transformation service built with Java and Spring Boot. Transform JSON data using JSLT (JSON Stylesheet Language Transformations) through multiple interfaces: CLI, REST API, and MCP (Model Context Protocol) integration for AI applications.

## 🚀 Features

### Core Capabilities
- **JSLT Transformations**: Transform JSON data using the powerful JSLT query language
- **Multiple Interfaces**: CLI, REST API, and MCP server for AI integration
- **Flexible Input/Output**: Support for JSON objects, strings, and configurable output formats
- **Enterprise Ready**: Built with Spring Boot, comprehensive logging, and error handling
- **AI Integration**: MCP protocol support for Claude Desktop and other AI applications

### Interface Options
- **CLI**: Command-line interface for batch processing and scripting
- **REST API**: HTTP endpoints for web applications and microservices
- **MCP Server**: Model Context Protocol integration for AI applications like Claude Desktop

## 📋 System Requirements

### Runtime Requirements
- **Java**: 17 or higher (tested with Java 17, 21, and 24)
- **Memory**: Minimum 512MB RAM (recommended 1GB+ for large files)
- **Storage**: 100MB for application, additional space for data processing

### Development Requirements
- **Java**: 17+ (OpenJDK or Oracle JDK)
- **Maven**: 3.6+ for building
- **Git**: For version control

### Supported Platforms
- **Operating Systems**: Linux, macOS, Windows
- **Deployment**: Docker, Kubernetes, standalone JAR

## 🛠️ Installation & Setup

### Quick Start (CLI)
```bash
# Clone the repository
git clone https://github.com/dionesiusap/data_transformer_service.git
cd data_transformer_service/jslt-transformer

# Build the project
mvn clean package -DskipTests

# Run CLI transformation
java -jar target/jslt-transformer-1.0-SNAPSHOT.jar \
  --input-file input.json \
  --query-file transform.jslt \
  --output-file result.json
```

### REST API Server
```bash
# Start the REST API server
mvn spring-boot:run

# The API will be available at http://localhost:8080
# API documentation: http://localhost:8080/swagger-ui.html
```

### MCP Server (AI Integration)
```bash
# Start in MCP mode for AI applications
mvn spring-boot:run -Dspring-boot.run.arguments="--mcp"

# Configure in Claude Desktop (see MCP Integration Guide)
```

## 🔧 Usage Examples

### CLI Usage
```bash
# Basic transformation
java -jar jslt-transformer.jar --input-file data.json --query-file query.jslt

# With output file
java -jar jslt-transformer.jar \
  --input-file customer-data.json \
  --query-file customer-transform.jslt \
  --output-file transformed-customers.json
```

### REST API Usage
```bash
# Transform JSON data
curl -X POST http://localhost:8080/api/transform \
  -H "Content-Type: application/json" \
  -d '{
    "jsonData": {"name": "John", "age": 30},
    "jsltQuery": ".name",
    "prettyPrint": true
  }'

# Health check
curl http://localhost:8080/health
```

### MCP Integration (Claude Desktop)
1. Start the MCP server: `mvn spring-boot:run -Dspring-boot.run.arguments="--mcp"`
2. Configure Claude Desktop with the MCP server
3. Use the `jslt_transform` tool in Claude conversations

## 📚 Documentation

### Core Documentation
- **[REQUIREMENTS.md](REQUIREMENTS.md)**: Detailed requirements and roadmap
- **[MCP Integration Guide](jslt-transformer/docs/mcp-integration-guide.md)**: Complete MCP setup and usage
- **[MCP Research Notes](docs/mcp-research-notes.md)**: Technical background on MCP protocol

### API Documentation
- **OpenAPI/Swagger**: Available at `/swagger-ui.html` when running REST API
- **Health Endpoints**: `/health`, `/health/readiness`, `/health/liveness`

## 🏗️ Architecture

### Technology Stack
- **Java 17+**: Modern Java with records, text blocks, and enhanced APIs
- **Spring Boot 3.2**: Enterprise framework with auto-configuration
- **[JSLT Library](https://github.com/schibsted/jslt)**: JSON query and transformation language
- **Jackson**: JSON processing and serialization
- **Maven**: Build and dependency management

### Project Structure
```
data_transformer_service/
├── README.md                    # This file
├── REQUIREMENTS.md              # Detailed requirements
├── docs/                        # Additional documentation
├── jslt-transformer/            # Main application
│   ├── src/main/java/          # Source code
│   ├── src/test/java/          # Unit tests
│   ├── src/main/resources/     # Configuration files
│   └── docs/                   # Module-specific docs
└── test-data/                  # Sample input/output files
```

### Key Components
- **Core Service**: JSLT transformation logic
- **CLI Interface**: Command-line application
- **REST Controller**: HTTP API endpoints
- **MCP Server**: Model Context Protocol implementation
- **Configuration**: Spring Boot configuration management

## 🚦 Development Status

### Completed Phases
- ✅ **Phase 1**: CLI Interface - Complete and validated
- ✅ **Phase 2**: REST API - Complete with flexible I/O
- ✅ **Phase 3**: MCP Integration - Complete with Claude Desktop support

### Current Phase
- 🔄 **Phase 4**: Production Readiness - Docker, monitoring, deployment

### Roadmap
- Database integration for logging and API keys
- Advanced authentication and authorization
- Performance optimization for large files
- Web UI for interactive transformations
- Additional AI platform integrations

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest="JsltTransformationServiceTest"

# Run manual MCP tests (Java 24 compatible)
mvn test -Dtest="McpServerManualTest"
```

### Test Coverage
- Unit tests for core transformation logic
- Integration tests for REST API endpoints
- Manual validation tests for MCP server
- CLI integration tests with real files

## 📄 License

This project is proprietary software developed for internal use. All rights reserved.

## 🆘 Support

### Getting Help
- **Issues**: Create GitHub issues for bugs and feature requests

### Known Limitations
- **Java 24 + Mockito**: Automated tests using mocks require Java 17-21
- **Large Files**: Memory usage scales with input size (optimization planned)
- **Concurrent Users**: Not yet optimized for high concurrency

### Troubleshooting
- **Port Conflicts**: Use `pkill -f "spring-boot:run"` to stop running servers
- **Memory Issues**: Increase JVM heap size with `-Xmx2g`
- **MCP Connection**: Ensure stdio transport is enabled in configuration

---

**Version**: 1.0-SNAPSHOT  
**Last Updated**: August 2025  
**Maintainer**: dionesiusap
