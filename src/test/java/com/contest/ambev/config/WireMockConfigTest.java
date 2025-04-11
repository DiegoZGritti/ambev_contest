package com.contest.ambev.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WireMockConfigTest {

    @InjectMocks
    private WireMockConfig wireMockConfig;

    @Mock
    private WireMockServer wireMockServer;

    @Test
    void setupWireMock_ShouldStartServer() {

        ReflectionTestUtils.setField(wireMockConfig, "wireMockServer", wireMockServer);

        WireMockServer result = wireMockConfig.wireMockServer();
        assertSame(wireMockServer, result);
    }

    @Test
    void tearDown_ShouldStopWireMockServer_IfRunning() {
        ReflectionTestUtils.setField(wireMockConfig, "wireMockServer", wireMockServer);
        when(wireMockServer.isRunning()).thenReturn(true);
        
        wireMockConfig.tearDown();
        
        verify(wireMockServer).isRunning();
        verify(wireMockServer).stop();
    }

    @Test
    void tearDown_ShouldNotStopWireMockServer_IfNotRunning() {
        ReflectionTestUtils.setField(wireMockConfig, "wireMockServer", wireMockServer);
        when(wireMockServer.isRunning()).thenReturn(false);
        
        wireMockConfig.tearDown();
        
        verify(wireMockServer).isRunning();
        verify(wireMockServer, never()).stop();
    }

    @Test
    void tearDown_ShouldDoNothing_IfWireMockServerIsNull() {
        ReflectionTestUtils.setField(wireMockConfig, "wireMockServer", null);
        
        assertDoesNotThrow(() -> wireMockConfig.tearDown());
    }
    
    @Test
    void wireMockServer_ShouldReturnWireMockServerInstance() {
        ReflectionTestUtils.setField(wireMockConfig, "wireMockServer", wireMockServer);
        
        WireMockServer result = wireMockConfig.wireMockServer();
        
        assertSame(wireMockServer, result);
    }
} 
