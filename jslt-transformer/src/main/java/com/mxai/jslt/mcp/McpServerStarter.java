package com.mxai.jslt.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Application runner that starts the MCP server when the application boots.
 * 
 * This component automatically starts the MCP server with stdio transport
 * when the application is run in MCP mode.
 */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class McpServerStarter implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServerStarter.class);
    
    private final McpServerService mcpServerService;
    private final McpServerConfiguration config;
    
    public McpServerStarter(McpServerService mcpServerService, McpServerConfiguration config) {
        this.mcpServerService = mcpServerService;
        this.config = config;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if MCP mode is requested
        boolean mcpMode = args.containsOption("mcp") || 
                         args.containsOption("mcp-server") ||
                         System.getProperty("mcp.mode", "false").equals("true");
        
        if (mcpMode && config.getStdio().isEnabled()) {
            logger.info("Starting application in MCP server mode");
            
            // Start MCP server with stdio transport
            // This will block and handle MCP requests via stdin/stdout
            mcpServerService.startStdioServer();
        } else if (config.isEnabled()) {
            logger.info("MCP server is configured but not started. Use --mcp flag or set mcp.mode=true to start MCP server");
        }
    }
}
