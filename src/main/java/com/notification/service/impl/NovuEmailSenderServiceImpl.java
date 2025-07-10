package com.notification.service.impl;

import co.novu.api.events.requests.TriggerEventRequest;
import co.novu.api.events.responses.EventResponse; // Corrected import
import co.novu.api.events.pojos.Subscriber; // Corrected import
import co.novu.sdk.Novu; // Corrected import
import co.novu.sdk.NovuConfig; // Corrected import
import com.notification.dto.EmailRequest;
import com.notification.exception.EmailSendingException;
import com.notification.service.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link EmailSenderService} that uses Novu to send emails.
 * This version is updated for Novu SDK `co.novu:novu-java:1.6.0`.
 */
@Service
public class NovuEmailSenderServiceImpl implements EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(NovuEmailSenderServiceImpl.class);

    private final Novu novu; // Correct SDK class
    private final NovuConfig novuConfig; // Correct SDK class, used for API key check

    @Value("${novu.workflow.trigger.id:default-email-workflow}")
    private String novuWorkflowTriggerId;

    public NovuEmailSenderServiceImpl(Novu novu, NovuConfig novuConfig) { // Correct constructor params
        this.novu = novu;
        this.novuConfig = novuConfig;
    }

    /**
     * Sends an email using the Novu service.
     *
     * @param request The {@link EmailRequest} containing email details.
     * @throws EmailSendingException if Novu fails to trigger the event or if the API key is not set.
     */
    @Override
    public void sendEmail(EmailRequest request) {
        if (novu == null || novuConfig == null || novuConfig.getApiKey() == null || novuConfig.getApiKey().isEmpty() || "YOUR_NOVU_API_KEY".equals(novuConfig.getApiKey())) {
            logger.error("Novu API key is not configured. Cannot send email.");
            throw new EmailSendingException("Novu service is not configured. API key missing.");
        }

        Map<String, Object> payload = new HashMap<>();
        if (request.getEmailVariables() != null) {
            payload.putAll(request.getEmailVariables());
        }
        payload.put("emailSubject", request.getSubject());
        payload.put("emailBody", request.getBody());
        payload.put("emailSignature", request.getSignature());

        List<Subscriber> toSubscribers = new ArrayList<>();
        Subscriber mainRecipient = new Subscriber();
        mainRecipient.setSubscriberId(request.getTo());
        mainRecipient.setEmail(request.getTo());
        toSubscribers.add(mainRecipient);

        TriggerEventRequest triggerEventRequest = new TriggerEventRequest();
        triggerEventRequest.setName(novuWorkflowTriggerId);
        triggerEventRequest.setPayload(payload);
        triggerEventRequest.setTo(toSubscribers);

        try {
            logger.info("Triggering Novu event '{}' for recipient: {}", novuWorkflowTriggerId, request.getTo());
            EventResponse response = novu.triggerEvent(triggerEventRequest); // Correct response type

            // Check response data carefully as per co.novu.api.events.responses.EventResponseData
            if (response == null || response.getData() == null || !Boolean.TRUE.equals(response.getData().getAcknowledged()) || !"triggered".equalsIgnoreCase(response.getData().getStatus())) {
                logger.error("Novu event trigger failed or was not acknowledged for {}. Response: {}", request.getTo(), response);
                throw new EmailSendingException("Failed to trigger Novu event for email to " + request.getTo() + ". Status: " + (response != null && response.getData() != null ? response.getData().getStatus() : "N/A"));
            }

            logger.info("Novu event triggered successfully for {}. TransactionId: {}", request.getTo(), response.getData().getTransactionId());

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                triggerForAdditionalRecipients(request.getCc(), payload, "CC");
            }
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                triggerForAdditionalRecipients(request.getBcc(), payload, "BCC");
            }

        } catch (Exception e) {
            logger.error("Error sending email via Novu to {}: {}", request.getTo(), e.getMessage(), e);
            throw new EmailSendingException("Error sending email via Novu to " + request.getTo() + ": " + e.getMessage(), e);
        }
    }

    private void triggerForAdditionalRecipients(List<String> recipientEmails, Map<String, Object> basePayload, String type) {
        for (String email : recipientEmails) {
            List<Subscriber> toSubscribers = new ArrayList<>();
            Subscriber recipient = new Subscriber(); // Correct Subscriber class
            recipient.setSubscriberId(email);
            recipient.setEmail(email);
            toSubscribers.add(recipient);

            TriggerEventRequest additionalTrigger = new TriggerEventRequest();
            additionalTrigger.setName(novuWorkflowTriggerId);
            additionalTrigger.setPayload(new HashMap<>(basePayload));
            additionalTrigger.setTo(toSubscribers);

            try {
                logger.info("Triggering Novu event '{}' for {} recipient: {}", novuWorkflowTriggerId, type, email);
                EventResponse response = novu.triggerEvent(additionalTrigger); // Correct response type

                if (response == null || response.getData() == null || !Boolean.TRUE.equals(response.getData().getAcknowledged()) || !"triggered".equalsIgnoreCase(response.getData().getStatus())) {
                    logger.error("Novu event trigger failed or was not acknowledged for {} recipient {}. Response: {}", type, email, response);
                } else {
                    logger.info("Novu event triggered successfully for {} recipient {}. TransactionId: {}", type, email, response.getData().getTransactionId());
                }
            } catch (Exception e) {
                logger.error("Error sending {} email via Novu to {}: {}", type, email, e.getMessage(), e);
            }
        }
    }
}
