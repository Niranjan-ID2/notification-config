package com.notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.EmailRequest;
import com.notification.service.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsEmailListenerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Spy // Using a real ObjectMapper instance
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Validator validator;

    @InjectMocks
    private SqsEmailListener sqsEmailListener;

    private EmailRequest validEmailRequest;
    private String validEmailRequestJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        validEmailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("SQS Test")
                .body("Message from SQS")
                .build();
        validEmailRequestJson = objectMapper.writeValueAsString(validEmailRequest);

        // Default behavior for validator: no violations
        when(validator.validate(any(EmailRequest.class))).thenReturn(Collections.emptySet());
    }

    @Test
    void receiveEmailRequest_success() {
        doNothing().when(emailSenderService).sendEmail(any(EmailRequest.class));

        assertDoesNotThrow(() -> sqsEmailListener.receiveEmailRequest(validEmailRequestJson, "msg-id-123", "timestamp"));

        verify(objectMapper).readValue(eq(validEmailRequestJson), eq(EmailRequest.class));
        verify(validator).validate(any(EmailRequest.class));
        verify(emailSenderService).sendEmail(any(EmailRequest.class));
    }

    @Test
    void receiveEmailRequest_jsonProcessingException() throws JsonProcessingException {
        String malformedJson = "{\"to\":\"test@example.com\", subject"; // Malformed

        // Mock objectMapper to throw JsonProcessingException for this specific malformed JSON.
        // Since objectMapper is a spy, we can mock specific methods.
        // However, it's easier to just pass malformed JSON and let the real method throw.
        // Forcing specific exception type from a mocked readValue:
        // when(objectMapper.readValue(eq(malformedJson), eq(EmailRequest.class))).thenThrow(JsonProcessingException.class);

        assertThrows(RuntimeException.class, () -> {
            sqsEmailListener.receiveEmailRequest(malformedJson, "msg-id-error", "timestamp");
        }, "SQS message deserialization error for messageId msg-id-error");

        verify(emailSenderService, never()).sendEmail(any());
    }

    @Test
    void receiveEmailRequest_validationFailed() {
        // Simulate validation failure
        Set<ConstraintViolation<EmailRequest>> violations = new HashSet<>();
        ConstraintViolation<EmailRequest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("to");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");
        violations.add(violation);

        when(validator.validate(any(EmailRequest.class))).thenReturn(violations);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sqsEmailListener.receiveEmailRequest(validEmailRequestJson, "msg-id-validation-fail", "timestamp");
        });

        assertTrue(exception.getMessage().contains("Invalid EmailRequest from SQS: to: must not be blank"));
        verify(emailSenderService, never()).sendEmail(any());
    }

    @Test
    void receiveEmailRequest_emailServiceThrowsException() {
        doThrow(new RuntimeException("Email service failure")).when(emailSenderService).sendEmail(any(EmailRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sqsEmailListener.receiveEmailRequest(validEmailRequestJson, "msg-id-service-fail", "timestamp");
        });

        assertTrue(exception.getMessage().contains("Generic error processing SQS messageId msg-id-service-fail"));
        assertEquals("Email service failure", exception.getCause().getMessage());

        verify(emailSenderService).sendEmail(any(EmailRequest.class));
    }
}
