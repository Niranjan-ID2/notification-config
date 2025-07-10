package com.notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.EmailRequest;
import com.notification.service.EmailSenderService;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Listens to an AWS SQS queue for messages to trigger email sending.
 */
@Component
public class SqsEmailListener {

    private static final Logger logger = LoggerFactory.getLogger(SqsEmailListener.class);

    private final EmailSenderService emailSenderService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Constructs an SqsEmailListener.
     *
     * @param emailSenderService Service to send emails.
     * @param objectMapper     For deserializing JSON messages from SQS.
     * @param validator        For validating the deserialized {@link EmailRequest}.
     */
    @Autowired
    public SqsEmailListener(EmailSenderService emailSenderService, ObjectMapper objectMapper, Validator validator) {
        this.emailSenderService = emailSenderService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Listens to the configured SQS queue for incoming messages.
     * Messages are expected to be JSON strings that can be deserialized into {@link EmailRequest}.
     *
     * @param message        The raw message content (JSON string) from SQS.
     * @param messageId      The SQS message ID, injected from the message headers.
     * @param approximateFirstReceiveTimestamp The approximate time the message was first received, for logging.
     */
    @SqsListener(value = "${cloud.aws.sqs.queue.name}", deletionPolicy = io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy.ON_SUCCESS)
    public void receiveEmailRequest(String message,
                                    @Header("MessageId") String messageId, // Standard SQS message attribute
                                    @Header(name = "ApproximateFirstReceiveTimestamp", required = false) String approximateFirstReceiveTimestamp) { // SQS attribute
        logger.info("Received SQS message ID: {}. ApproxFirstReceiveTimestamp: {}. Payload: {}", messageId, approximateFirstReceiveTimestamp, message);

        try {
            EmailRequest emailRequest = objectMapper.readValue(message, EmailRequest.class);
            logger.info("Deserialized SQS message to EmailRequest for recipient: {}", emailRequest.getTo());

            Set<ConstraintViolation<EmailRequest>> violations = validator.validate(emailRequest);
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                                                 .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                                 .collect(Collectors.joining(", "));
                logger.error("Validation failed for EmailRequest from SQS message ID {}: {}. Dropping message.", messageId, errorMessages);
                // Message will not be deleted due to SqsListener. оригиналаSqsMessageDeletionPolicy.ON_SUCCESS
                // if an exception is thrown. If we want to discard it, we should not throw.
                // Consider moving to DLQ if validation fails permanently. For now, log and it won't be retried by this listener instance after exception.
                // To ensure it's not reprocessed and potentially stuck, either ensure a DLQ is configured on SQS,
                // or catch this and don't rethrow if the message is truly unprocessable.
                // For now, we'll let it throw, assuming DLQ handles persistent bad messages.
                throw new IllegalArgumentException("Invalid EmailRequest from SQS: " + errorMessages);
            }

            emailSenderService.sendEmail(emailRequest);
            logger.info("Successfully processed SQS message ID {} and triggered email for: {}", messageId, emailRequest.getTo());
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize SQS message ID {} into EmailRequest. Message content: {}. Error: {}", messageId, message, e.getMessage(), e);
            // This is likely a malformed message. It might need to go to a DLQ.
            // Throwing an exception will make SQS redeliver it until maxReceiveCount, then DLQ (if configured).
            throw new RuntimeException("SQS message deserialization error for messageId " + messageId, e);
        } catch (IllegalArgumentException e) {
            // Validation error already logged. Rethrow to ensure SQS handles it (e.g., DLQ).
            throw e;
        } catch (Exception e) {
            logger.error("Error processing SQS message ID {} for email request. Error: {}", messageId, e.getMessage(), e);
            // For other errors (e.g., EmailSendingException), rethrow so SQS can retry or DLQ.
            throw new RuntimeException("Generic error processing SQS messageId " + messageId, e);
        }
    }
}
