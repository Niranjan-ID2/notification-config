package com.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.EmailRequest;
import com.notification.exception.EmailSendingException;
import com.notification.service.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailSenderService emailSenderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendEmail_success() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Hello World")
                .cc(Collections.singletonList("cc@example.com"))
                .emailVariables(Map.of("user", "John"))
                .build();

        doNothing().when(emailSenderService).sendEmail(any(EmailRequest.class));

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Email request accepted for processing."));

        verify(emailSenderService).sendEmail(any(EmailRequest.class));
    }

    @Test
    void sendEmail_validationError_missingTo() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                // .to("test@example.com") // 'to' is missing
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.to").value("To email address cannot be empty."));
    }

    @Test
    void sendEmail_validationError_invalidEmailFormat() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("invalid-email")
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.to").value("Invalid 'to' email address format."));
    }

    @Test
    void sendEmail_validationError_invalidCcEmailFormat() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("valid@example.com")
                .cc(Collections.singletonList("invalid-cc-email"))
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors['cc[0]']").value("Invalid 'cc' email address format."));
    }


    @Test
    void sendEmail_serviceThrowsEmailSendingException() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Failure Test")
                .build();

        doThrow(new EmailSendingException("Novu unavailable")).when(emailSenderService).sendEmail(any(EmailRequest.class));

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error occurred while sending email."))
                .andExpect(jsonPath("$.error").value("EmailSendingException"));
    }

    @Test
    void sendEmail_serviceThrowsUnexpectedException() throws Exception {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Failure Test")
                .build();

        // Simulate a generic runtime exception from the service layer
        doThrow(new RuntimeException("Unexpected database error")).when(emailSenderService).sendEmail(any(EmailRequest.class));

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred while processing the email request."));
                // The GlobalExceptionHandler's generic handler for Exception.class will be invoked by Spring,
                // but the controller's own try-catch block for Exception might catch it first if the service call is direct.
                // Let's adjust the expectation based on the controller's direct catch block.
    }
}
