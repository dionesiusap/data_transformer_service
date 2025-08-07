package com.mxai.jslt.mcp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for MCP (Model Context Protocol) server.
 * 
 * This configuration enables the JSLT transformation service to be exposed
 * as an MCP server that AI models can connect to and invoke tools.
 */
@Configuration
@ConfigurationProperties(prefix = "mcp.server")
public class McpServerConfiguration {
    
    /**
     * Whether MCP server is enabled
     */
    private boolean enabled = true;
    
    /**
     * Server name for MCP identification
     */
    private String name = "jslt-transformer";
    
    /**
     * Server version
     */
    private String version = "1.0.0";
    
    /**
     * Server description
     */
    private String description = "JSLT JSON Transformation Service - Transform JSON data using JSLT queries";
    
    /**
     * Stdio transport configuration
     */
    private StdioTransport stdio = new StdioTransport();
    
    /**
     * HTTP transport configuration
     */
    private HttpTransport http = new HttpTransport();
    
    // Getters and setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public StdioTransport getStdio() {
        return stdio;
    }
    
    public void setStdio(StdioTransport stdio) {
        this.stdio = stdio;
    }
    
    public HttpTransport getHttp() {
        return http;
    }
    
    public void setHttp(HttpTransport http) {
        this.http = http;
    }
    
    /**
     * Stdio transport configuration for Claude Desktop integration
     */
    public static class StdioTransport {
        private boolean enabled = true;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * HTTP transport configuration for web-based AI clients
     */
    public static class HttpTransport {
        private boolean enabled = false;
        private int port = 8081;
        private String path = "/mcp";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
    }
}
