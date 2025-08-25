package com.mxai.jslt.mcp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot application for JSLT MCP Server using STDIO transport.
 * 
 * This application provides Model Context Protocol (MCP) server functionality
 * for JSLT transformations, specifically designed for integration with 
 * Claude Desktop and other MCP clients using STDIO transport.
 * 
 * Key Features:
 * - STDIO transport for Claude Desktop integration
 * - JSLT transformation tool exposed via MCP
 * - Spring Boot auto-configuration for MCP server
 * - File-based logging (console logging disabled for STDIO compatibility)
 * 
 * Usage:
 * java -jar jslt-transformer-1.0-SNAPSHOT-mcp.jar
 * 
 * @author JSLT Transformer Team
 * @version 1.0
 * @since 2025-08-08
 */
@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
@ComponentScan(basePackages = {
    "com.mxai.jslt.core",
    "com.mxai.jslt.mcp"
})
public class SpringMcpServerApplication {
    
    public static void main(String[] args) {
        // Configure system properties for STDIO transport
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("logging.config", "classpath:logback-mcp.xml");
        
        try {
            SpringApplication app = new SpringApplication(SpringMcpServerApplication.class);
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
            app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
            app.run(args);
        } catch (Exception e) {
            // Silent failure - any output to stderr/stdout would corrupt MCP JSON-RPC protocol
            System.exit(1);
        }
    }
}
