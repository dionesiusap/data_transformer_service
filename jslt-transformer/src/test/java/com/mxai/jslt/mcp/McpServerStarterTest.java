package com.mxai.jslt.mcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * Unit tests for McpServerStarter.
 * Tests MCP server startup logic and conditional execution.
 */
@ExtendWith(MockitoExtension.class)
class McpServerStarterTest {

    @Mock
    private McpServerService mcpServerService;

    @Mock
    private ApplicationArguments applicationArguments;

    private McpServerService mockMcpServerService;
    private McpServerConfiguration mockConfig;
    private McpServerStarter mcpServerStarter;

    @BeforeEach
    void setUp() {
        mockMcpServerService = mock(McpServerService.class);
        mockConfig = mock(McpServerConfiguration.class);
        mcpServerStarter = new McpServerStarter(mockMcpServerService, mockConfig);
    }

    @Test
    void testRunWithMcpModeArgument() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(true);

        // Act
        mcpServerStarter.run(applicationArguments);

        // Assert
        verify(mcpServerService, times(1)).startStdioServer();
    }

    @Test
    void testRunWithMcpModeSystemProperty() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(false);
        System.setProperty("mcp.mode", "true");

        try {
            // Act
            mcpServerStarter.run(applicationArguments);

            // Assert
            verify(mcpServerService, times(1)).startStdioServer();
        } finally {
            // Clean up
            System.clearProperty("mcp.mode");
        }
    }

    @Test
    void testRunWithoutMcpMode() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(false);
        System.clearProperty("mcp.mode");

        // Act
        mcpServerStarter.run(applicationArguments);

        // Assert
        verify(mcpServerService, never()).startStdioServer();
    }

    @Test
    void testRunWithMcpModeSystemPropertyFalse() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(false);
        System.setProperty("mcp.mode", "false");

        try {
            // Act
            mcpServerStarter.run(applicationArguments);

            // Assert
            verify(mcpServerService, never()).startStdioServer();
        } finally {
            // Clean up
            System.clearProperty("mcp.mode");
        }
    }

    @Test
    void testRunWithMcpModeSystemPropertyEmpty() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(false);
        System.setProperty("mcp.mode", "");

        try {
            // Act
            mcpServerStarter.run(applicationArguments);

            // Assert
            verify(mcpServerService, never()).startStdioServer();
        } finally {
            // Clean up
            System.clearProperty("mcp.mode");
        }
    }

    @Test
    void testRunHandlesServiceException() throws Exception {
        // Arrange
        when(applicationArguments.containsOption("mcp")).thenReturn(true);
        doThrow(new RuntimeException("Test exception")).when(mcpServerService).startStdioServer();

        // Act & Assert - should not throw exception
        mcpServerStarter.run(applicationArguments);

        // Verify that the service was called despite the exception
        verify(mcpServerService, times(1)).startStdioServer();
    }
}
