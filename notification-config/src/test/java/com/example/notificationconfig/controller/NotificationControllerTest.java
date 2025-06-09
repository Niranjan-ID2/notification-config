package com.example.notificationconfig.controller;

import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never; // Added import
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest { // Renamed class

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void triggerNotificationEvent_validRequest_shouldReturnOk() throws Exception {
        // Arrange
        EventRequest validRequest = new EventRequest("test-event", "sub-123", "test@example.com", null, new HashMap<>());
        doNothing().when(notificationService).sendNotificationEvent(any(EventRequest.class));

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/notify/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        // Assert
        result.andExpect(status().isOk())
              .andExpect(content().string("Notification Triggered"));
        verify(notificationService).sendNotificationEvent(any(EventRequest.class));
    }

    @Test
    void triggerNotificationEvent_missingEventName_shouldReturnBadRequest() throws Exception {
        // Arrange
        EventRequest invalidRequest = new EventRequest(null, "sub-123", "test@example.com", null, new HashMap<>());

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/notify/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Validation Failed"))
              .andExpect(jsonPath("$.message").value("Event name is required"));
        verify(notificationService, never()).sendNotificationEvent(any(EventRequest.class));
    }

    @Test
    void triggerNotificationEvent_missingSubscriberId_shouldReturnBadRequest() throws Exception {
        // Arrange
        EventRequest invalidRequest = new EventRequest("test-event", null, "test@example.com", null, new HashMap<>());

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/notify/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Validation Failed"))
              .andExpect(jsonPath("$.message").value("Subscriber ID is required"));
        verify(notificationService, never()).sendNotificationEvent(any(EventRequest.class));
    }

    @Test
    void triggerNotificationEvent_invalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        EventRequest invalidRequest = new EventRequest("test-event", "sub-123", "not-an-email", null, new HashMap<>());

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/notify/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Validation Failed"))
              .andExpect(jsonPath("$.message").value("Email should be valid"));
        verify(notificationService, never()).sendNotificationEvent(any(EventRequest.class));
    }

     @Test
    void triggerNotificationEvent_missingEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        EventRequest invalidRequest = new EventRequest("test-event", "sub-123", "", null, new HashMap<>());

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/notify/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Validation Failed"))
              .andExpect(jsonPath("$.message").value("Email is required")); // Or "Email is required, Email should be valid" if both are triggered
        verify(notificationService, never()).sendNotificationEvent(any(EventRequest.class));
    }
}
