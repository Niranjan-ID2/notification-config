package com.example.notificationconfig.controller;

import com.example.notificationconfig.dto.EmailRequest;
import com.example.notificationconfig.service.EmailNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(EmailController.class)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailNotificationService emailNotificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void triggerEmail_validRequest_shouldReturnOk() throws Exception {
        EmailRequest validRequest = new EmailRequest(
                "test@example.com",
                Collections.singletonList("recipient@example.com"),
                null,
                null,
                "Test Subject",
                "Test Body",
                "Test Signature",
                null
        );

        mockMvc.perform(post("/api/v1/email/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(emailNotificationService, times(1)).sendEmail(any(EmailRequest.class));
    }

    @Test
    void triggerEmail_invalidRequest_nullEmailFrom_shouldReturnBadRequest() throws Exception {
        EmailRequest invalidRequest = new EmailRequest(
                null, // Invalid: email 'from' is null
                Collections.singletonList("recipient@example.com"),
                null,
                null,
                "Test Subject",
                "Test Body",
                "Test Signature",
                null
        );

        mockMvc.perform(post("/api/v1/email/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email 'from' field is mandatory")); // From @NotBlank in EmailRequest

        verify(emailNotificationService, times(0)).sendEmail(any(EmailRequest.class));
    }

    @Test
    void triggerEmail_invalidRequest_emptyEmailFrom_shouldReturnBadRequest() throws Exception {
        EmailRequest invalidRequest = new EmailRequest(
                "", // Invalid: email 'from' is blank
                Collections.singletonList("recipient@example.com"),
                null,
                null,
                "Test Subject",
                "Test Body",
                "Test Signature",
                null
        );

        mockMvc.perform(post("/api/v1/email/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Email 'from' field is mandatory"));


        verify(emailNotificationService, times(0)).sendEmail(any(EmailRequest.class));
    }
}
