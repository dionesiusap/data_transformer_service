package com.mxai.jslt.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for McpServerConfiguration.
 * Tests configuration property binding and validation.
 */
@SpringBootTest(classes = McpServerConfiguration.class)
@EnableConfigurationProperties(McpServerConfiguration.class)
class McpServerConfigurationTest {

    @Test
    void testConfigurationBinding(@Autowired McpServerConfiguration config) {
        // Assert main properties
        assertTrue(config.isEnabled());
        assertEquals("Test Server", config.getName());
        assertEquals("2.0.0", config.getVersion());
        assertEquals("Test Description", config.getDescription());
        
        // Assert transport properties
        assertNotNull(config.getStdio());
        assertNotNull(config.getHttp());
        assertTrue(config.getStdio().isEnabled());
        assertFalse(config.getHttp().isEnabled());
        assertEquals(9090, config.getHttp().getPort());
    }

    @Test
    void testDefaultValues(@Autowired McpServerConfiguration config) {
        // Assert default values
        assertFalse(config.isEnabled());
        assertEquals("JSLT Transformation Service", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("MCP server for JSLT JSON transformations", config.getDescription());
        
        // Assert default transport values
        assertNotNull(config.getStdio());
        assertNotNull(config.getHttp());
        assertTrue(config.getStdio().isEnabled());
        assertFalse(config.getHttp().isEnabled());
        assertEquals(8081, config.getHttp().getPort());
    }

    @Test
    void testTransportConfigurationDefaults() {
        McpServerConfiguration config = new McpServerConfiguration();
        
        assertNotNull(config.getStdio());
        assertTrue(config.getStdio().isEnabled());
        
        assertNotNull(config.getHttp());
        assertFalse(config.getHttp().isEnabled());
        assertEquals(8081, config.getHttp().getPort());
    }

    @Test
    void testStdioConfigurationDefaults() {
        McpServerConfiguration.StdioTransport stdio = new McpServerConfiguration.StdioTransport();
        assertTrue(stdio.isEnabled());
    }

    @Test
    void testHttpConfigurationDefaults() {
        McpServerConfiguration.HttpTransport http = new McpServerConfiguration.HttpTransport();
        assertFalse(http.isEnabled());
        assertEquals(8081, http.getPort());
    }

    @Test
    void testConfigurationSetters() {
        McpServerConfiguration config = new McpServerConfiguration();
        
        // Test main property setters
        config.setEnabled(true);
        config.setName("Custom Server");
        config.setVersion("3.0.0");
        config.setDescription("Custom Description");
        
        assertTrue(config.isEnabled());
        assertEquals("Custom Server", config.getName());
        assertEquals("3.0.0", config.getVersion());
        assertEquals("Custom Description", config.getDescription());
        
        // Test transport configuration
        McpServerConfiguration.StdioTransport stdio = new McpServerConfiguration.StdioTransport();
        config.setStdio(stdio);
        assertSame(stdio, config.getStdio());
    }

    @Test
    void testStdioConfigurationSetters() {
        McpServerConfiguration.StdioTransport stdio = new McpServerConfiguration.StdioTransport();
        stdio.setEnabled(false);
        assertFalse(stdio.isEnabled());
    }

    @Test
    void testHttpConfigurationSetters() {
        McpServerConfiguration.HttpTransport http = new McpServerConfiguration.HttpTransport();
        http.setEnabled(true);
        http.setPort(9999);
        
        assertTrue(http.isEnabled());
        assertEquals(9999, http.getPort());
    }

    @Test
    void testTransportConfigurationSetters() {
        McpServerConfiguration config = new McpServerConfiguration();
        
        McpServerConfiguration.StdioTransport stdio = new McpServerConfiguration.StdioTransport();
        stdio.setEnabled(false);
        config.setStdio(stdio);
        
        McpServerConfiguration.HttpTransport http = new McpServerConfiguration.HttpTransport();
        http.setEnabled(true);
        http.setPort(8888);
        config.setHttp(http);
        
        assertSame(stdio, config.getStdio());
        assertSame(http, config.getHttp());
        assertFalse(config.getStdio().isEnabled());
        assertTrue(config.getHttp().isEnabled());
        assertEquals(8888, config.getHttp().getPort());
    }
}
