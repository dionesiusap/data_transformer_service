# Decision Log

This document tracks key architectural and implementation decisions made during the development of the JSLT Transformation Service.

## 2025-08-08: MCP Standalone Architecture

### Context
User requested separation of MCP server from Spring Boot to have 3 distinct executables: CLI, REST, and MCP. The goal is cleaner architecture and simpler Claude Desktop integration.

### Decision: Single Project Architecture (Option A)
**Chosen:** Keep all interfaces in a single Maven project with shared core classes.

**Alternatives Considered:**
- Multi-module Maven project with separate modules for CLI, REST, MCP, and core

**Rationale:**
- Current codebase already well-structured with organized packages
- Simpler build process and maintenance
- Easier development and debugging
- Project size doesn't justify multi-module complexity
- JAR size differences not critical for this use case

### Decision: Command-Line Configuration
**Chosen:** Use command-line arguments with sensible defaults for MCP server configuration.

**Alternatives Considered:**
- Properties files
- Environment variables
- Spring Boot configuration properties

**Rationale:**
- Simpler Claude Desktop integration (no complex property passing)
- No dependency on Spring Boot configuration system
- Clear and explicit configuration

### Decision: File-Only Logging for MCP
**Chosen:** Configure MCP server to use file-only logging, disable console output.

**Rationale:**
- **CRITICAL:** MCP uses stdio transport (stdin/stdout) for JSON-RPC communication
- Console logging would corrupt MCP protocol messages
- File logging preserves debugging capability without protocol interference

### Decision: Consistent Error Format
**Chosen:** Keep the same TransformationResult error format across CLI, REST, and MCP interfaces.

**Rationale:**
- Consistent user experience
- Single error handling system to maintain
- Compatible tooling and parsing logic

### Implementation Target
- **CLI Executable:** `jslt-transformer-1.0-SNAPSHOT-cli.jar` (existing)
- **REST Executable:** `jslt-transformer-1.0-SNAPSHOT-spring-boot.jar` (existing)
- **MCP Executable:** `jslt-transformer-1.0-SNAPSHOT-mcp.jar` (new)
- **Entry Point:** `McpServerMain.main()` (new)
- **Dependencies:** No Spring Boot at runtime for MCP

---

## Template for Future Decisions

### [Date]: [Decision Title]

**Context:** Brief description of the problem or requirement

**Decision:** What was decided

**Alternatives Considered:** Other options that were evaluated

**Rationale:** Why this decision was made

**Impact:** Expected consequences or benefits
