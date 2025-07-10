package com.notification.service.impl;

// Updated imports for co.novu SDK
import co.novu.api.events.pojos.Subscriber;
import co.novu.api.events.requests.TriggerEventRequest;
import co.novu.api.events.responses.EventResponse;
import co.novu.api.events.responses.EventResponseData;
import co.novu.sdk.Novu;
import co.novu.sdk.NovuConfig;

import com.notification.dto.EmailRequest;
import com.notification.exception.EmailSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NovuEmailSenderServiceImplTest {

    @Mock
    private Novu novuMock;

    @Mock
    private NovuConfig novuSdkConfigMock; // This is co.novu.sdk.NovuConfig

    private NovuEmailSenderServiceImpl novuEmailSenderService;

    private final String testWorkflowTriggerId = "test-workflow";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Manually instantiate the service with mocked dependencies
        novuEmailSenderService = new NovuEmailSenderServiceImpl(novuMock, novuSdkConfigMock);

        when(novuSdkConfigMock.getApiKey()).thenReturn("test-api-key");
        ReflectionTestUtils.setField(novuEmailSenderService, "novuWorkflowTriggerId", testWorkflowTriggerId);
    }

    @Test
    void sendEmail_success_mainRecipientOnly() {
        EmailRequest request = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .emailVariables(Map.of("name", "Tester"))
                .build();

        EventResponse mockResponse = new EventResponse();
        EventResponseData responseData = new EventResponseData();
        responseData.setStatus("triggered");
        responseData.setTransactionId("tx_123");
        responseData.setAcknowledged(true); // Correct place for acknowledged
        mockResponse.setData(responseData);

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(mockResponse);

        novuEmailSenderService.sendEmail(request);

        ArgumentCaptor<TriggerEventRequest> captor = ArgumentCaptor.forClass(TriggerEventRequest.class);
        verify(novuMock, times(1)).triggerEvent(captor.capture());

        TriggerEventRequest triggeredRequest = captor.getValue();
        assertEquals(testWorkflowTriggerId, triggeredRequest.getName());
        assertNotNull(triggeredRequest.getTo());
        assertEquals(1, triggeredRequest.getTo().size());
        assertEquals("test@example.com", triggeredRequest.getTo().get(0).getEmail());
        assertEquals("test@example.com", triggeredRequest.getTo().get(0).getSubscriberId()); // Assuming email is subscriberId
        assertEquals("Tester", triggeredRequest.getPayload().get("name"));
        assertEquals("Test Subject", triggeredRequest.getPayload().get("emailSubject"));
    }

    @Test
    void sendEmail_success_withCcAndBcc() {
        EmailRequest request = EmailRequest.builder()
                .to("to@example.com")
                .cc(Arrays.asList("cc1@example.com", "cc2@example.com"))
                .bcc(Collections.singletonList("bcc@example.com"))
                .subject("Test Subject CC BCC")
                .body("Test Body")
                .build();

        EventResponse mockResponse = new EventResponse();
        EventResponseData responseData = new EventResponseData();
        responseData.setStatus("triggered");
        responseData.setTransactionId("tx_123_variant"); // Ensure unique transaction IDs if necessary for distinction
        responseData.setAcknowledged(true);
        mockResponse.setData(responseData);

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(mockResponse);

        novuEmailSenderService.sendEmail(request);

        ArgumentCaptor<TriggerEventRequest> captor = ArgumentCaptor.forClass(TriggerEventRequest.class);
        verify(novuMock, times(4)).triggerEvent(captor.capture());

        List<TriggerEventRequest> allRequests = captor.getAllValues();

        assertTrue(allRequests.stream().anyMatch(r -> "to@example.com".equals(r.getTo().get(0).getEmail())));
        assertTrue(allRequests.stream().anyMatch(r -> "cc1@example.com".equals(r.getTo().get(0).getEmail())));
        assertTrue(allRequests.stream().anyMatch(r -> "cc2@example.com".equals(r.getTo().get(0).getEmail())));
        assertTrue(allRequests.stream().anyMatch(r -> "bcc@example.com".equals(r.getTo().get(0).getEmail())));

        allRequests.forEach(r -> {
            assertEquals(testWorkflowTriggerId, r.getName());
            assertEquals("Test Subject CC BCC", r.getPayload().get("emailSubject"));
            assertEquals(1, r.getTo().size());
        });
    }

    @Test
    void sendEmail_novuApiKeyNotConfigured_throwsEmailSendingException() {
        when(novuSdkConfigMock.getApiKey()).thenReturn(null);

        EmailRequest request = EmailRequest.builder().to("test@example.com").build();

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertEquals("Novu service is not configured. API key missing.", exception.getMessage());
        verify(novuMock, never()).triggerEvent(any());
    }

    @Test
    void sendEmail_novuReturnsFailure_notAcknowledged_throwsEmailSendingException() {
        EmailRequest request = EmailRequest.builder().to("test@example.com").subject("Test").build();

        EventResponse mockResponse = new EventResponse();
        EventResponseData responseData = new EventResponseData();
        responseData.setAcknowledged(false);
        responseData.setStatus("error"); // Or some other non-triggered status
        mockResponse.setData(responseData);

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(mockResponse);

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertTrue(exception.getMessage().contains("Failed to trigger Novu event for email to test@example.com"));
    }
     @Test
    void sendEmail_novuReturnsNullResponse_throwsEmailSendingException() {
        EmailRequest request = EmailRequest.builder().to("test@example.com").subject("Test").build();

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(null);

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertTrue(exception.getMessage().contains("Failed to trigger Novu event for email to test@example.com"));
    }

    @Test
    void sendEmail_novuReturnsNullResponseData_throwsEmailSendingException() {
        EmailRequest request = EmailRequest.builder().to("test@example.com").subject("Test").build();
        EventResponse mockResponse = new EventResponse();
        mockResponse.setData(null); // Explicitly set data to null

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(mockResponse);

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertTrue(exception.getMessage().contains("Failed to trigger Novu event for email to test@example.com"));
    }


    @Test
    void sendEmail_novuReturnsFailure_statusNotTriggered_throwsEmailSendingException() {
        EmailRequest request = EmailRequest.builder().to("test@example.com").subject("Test").build();

        EventResponse mockResponse = new EventResponse();
        EventResponseData responseData = new EventResponseData();
        responseData.setAcknowledged(true);
        responseData.setStatus("processed");
        mockResponse.setData(responseData);

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenReturn(mockResponse);

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertTrue(exception.getMessage().contains("Failed to trigger Novu event for email to test@example.com"));
    }

    @Test
    void sendEmail_novuThrowsException_throwsEmailSendingException() {
        EmailRequest request = EmailRequest.builder().to("test@example.com").subject("Test").build();

        when(novuMock.triggerEvent(any(TriggerEventRequest.class))).thenThrow(new RuntimeException("Novu internal error"));

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            novuEmailSenderService.sendEmail(request);
        });
        assertTrue(exception.getMessage().contains("Error sending email via Novu to test@example.com"));
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Novu internal error", exception.getCause().getMessage());
    }

    @Test
    void sendEmail_partialFailure_forCcRecipient_doesNotStopProcessing() {
        EmailRequest request = EmailRequest.builder()
                .to("to@example.com")
                .cc(Arrays.asList("cc.success@example.com", "cc.fail@example.com"))
                .subject("Test Partial Failure")
                .build();

        EventResponse successResponse = new EventResponse();
        EventResponseData successData = new EventResponseData();
        successData.setStatus("triggered");
        successData.setTransactionId("tx_success");
        successData.setAcknowledged(true);
        successResponse.setData(successData);

        EventResponse failureResponse = new EventResponse();
        EventResponseData failureData = new EventResponseData();
        failureData.setAcknowledged(false);
        failureData.setStatus("error");
        failureResponse.setData(failureData);

        when(novuMock.triggerEvent(argThat(argument -> "to@example.com".equals(argument.getTo().get(0).getEmail()))))
                .thenReturn(successResponse);
        when(novuMock.triggerEvent(argThat(argument -> "cc.success@example.com".equals(argument.getTo().get(0).getEmail()))))
                .thenReturn(successResponse);
        when(novuMock.triggerEvent(argThat(argument -> "cc.fail@example.com".equals(argument.getTo().get(0).getEmail()))))
                .thenReturn(failureResponse);

        assertDoesNotThrow(() -> novuEmailSenderService.sendEmail(request));

        verify(novuMock, times(3)).triggerEvent(any(TriggerEventRequest.class));
    }
}
