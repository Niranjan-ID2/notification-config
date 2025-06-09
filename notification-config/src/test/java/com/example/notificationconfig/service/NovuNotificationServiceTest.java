package com.example.notificationconfig.service;

import com.example.notificationconfig.client.NovuClient;
import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.dto.TriggerEventRequest;
import com.example.notificationconfig.mapper.EventRequestMapper;
import com.example.notificationconfig.serviceImpl.NovuNotificationService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NovuNotificationServiceTest {

    @Mock
    private NovuClient novuClient;

    @Mock
    private EventRequestMapper eventRequestMapper;

    @InjectMocks
    private NovuNotificationService novuNotificationService;

    private EventRequest eventRequest;
    private TriggerEventRequest triggerEventRequest;

    @BeforeEach
    void setUp() {
        eventRequest = new EventRequest("test-event", "sub-123", "test@example.com", "1234567890", new HashMap<>());
        triggerEventRequest = TriggerEventRequest.builder()
                .name("test-event")
                .to(TriggerEventRequest.To.builder().subscriberId("sub-123").email("test@example.com").build())
                .payload(new HashMap<>())
                .build();
    }

    @Test
    void sendNotificationEvent_success() {
        // Arrange
        when(eventRequestMapper.toTriggerEventRequest(eventRequest)).thenReturn(triggerEventRequest);
        doNothing().when(novuClient).triggerEvent(triggerEventRequest);

        // Act
        novuNotificationService.sendNotificationEvent(eventRequest);

        // Assert
        verify(eventRequestMapper, times(1)).toTriggerEventRequest(eventRequest);
        verify(novuClient, times(1)).triggerEvent(triggerEventRequest);
    }

    @Test
    void sendNotificationEvent_novuClientThrowsFeignException_shouldPropagateException() {
        // Arrange
        when(eventRequestMapper.toTriggerEventRequest(eventRequest)).thenReturn(triggerEventRequest);
        FeignException mockException = mock(FeignException.class); // Using a mock FeignException
        // when(mockException.getMessage()).thenReturn("Novu API error"); // Unnecessary stubbing
        doThrow(mockException).when(novuClient).triggerEvent(triggerEventRequest);

        // Act & Assert
        assertThrows(FeignException.class, () -> {
            novuNotificationService.sendNotificationEvent(eventRequest);
        });

        verify(eventRequestMapper, times(1)).toTriggerEventRequest(eventRequest);
        verify(novuClient, times(1)).triggerEvent(triggerEventRequest);
    }

    @Test
    void sendNotificationEvent_eventRequestMapperCalledWithCorrectData() {
        // Arrange
        when(eventRequestMapper.toTriggerEventRequest(any(EventRequest.class))).thenReturn(triggerEventRequest);

        // Act
        novuNotificationService.sendNotificationEvent(eventRequest);

        // Assert
        verify(eventRequestMapper).toTriggerEventRequest(eventRequest);
    }

    @Test
    void sendNotificationEvent_novuClientCalledWithCorrectData() {
        // Arrange
        when(eventRequestMapper.toTriggerEventRequest(eventRequest)).thenReturn(triggerEventRequest);

        // Act
        novuNotificationService.sendNotificationEvent(eventRequest);

        // Assert
        verify(novuClient).triggerEvent(triggerEventRequest);
    }

    @Test
    void sendNotificationEvent_nullEventRequest_throwsNullPointerException() {
        // It's good practice for mappers or services to handle null inputs gracefully,
        // often by throwing IllegalArgumentException or NullPointerException.
        // Assuming EventRequestMapper would throw an NPE if eventRequest is null during mapping.
        when(eventRequestMapper.toTriggerEventRequest(null)).thenThrow(NullPointerException.class);

        assertThrows(NullPointerException.class, () -> {
            novuNotificationService.sendNotificationEvent(null);
        });

        verify(novuClient, never()).triggerEvent(any());
    }
}
