package com.openjiuwen.studio.agent.space.app.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class DREventSourceListenerFactoryTest {
    @InjectMocks
    private DREventSourceListenerFactory dREventSourceListenerFactory;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_create_should_return_not_null() throws Exception {
        // Given
        AgentBuilderDispatchVo AgentBuilderDispatchVo = new AgentBuilderDispatchVo();

        Runnable onClosedCallback = () -> {};
        // When
        DREventSourceListener result = dREventSourceListenerFactory.create(AgentBuilderDispatchVo, onClosedCallback);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_create_should_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // When
            DREventSourceListener result = dREventSourceListenerFactory.create(null, null);
        });
    }
}