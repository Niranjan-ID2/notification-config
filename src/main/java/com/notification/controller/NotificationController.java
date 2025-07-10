package com.notification.controller;

import com.notification.dto.EmailRequest;
import com.notification.service.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST Controller for handling notification requests, such as sending emails.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final EmailSenderService emailSenderService;

    /**
     * Constructs a NotificationController with the necessary EmailSenderService.
     *
     * @param emailSenderService The service responsible for sending emails.
     */
    @Autowired
    public NotificationController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    /**
     * API endpoint to trigger sending an email.
     * Accepts an {@link EmailRequest} and uses the {@link EmailSenderService} to dispatch the email.
     *
     * @param emailRequest The {@link EmailRequest} containing details for the email to be sent.
     *                     The request body is validated based on annotations in {@link EmailRequest}.
     * @return A {@link ResponseEntity} indicating the outcome of the operation.
     *         Returns HTTP 202 (Accepted) if the email request is successfully processed for sending.
     *         Returns HTTP 400 (Bad Request) if the input validation fails.
     *         Returns HTTP 500 (Internal Server Error) if an unexpected error occurs during email processing.
     */
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("Received request to send email to: {}", emailRequest.getTo());
        try {
            emailSenderService.sendEmail(emailRequest);
            logger.info("Email request for {} processed successfully.", emailRequest.getTo());
            // Using 202 Accepted as email sending is often asynchronous.
            // The request is accepted for processing, not necessarily sent and delivered instantly.
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Email request accepted for processing.");
        } catch (Exception e) {
            // Specific exceptions like EmailSendingException or InvalidRequestException
            // will be handled by the GlobalExceptionHandler.
            // This catch block is for any other unexpected exceptions from the service layer,
            // though ideally, services should wrap their exceptions.
            logger.error("Error processing email request for {}: {}", emailRequest.getTo(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An unexpected error occurred while processing the email request.");
        }
    }
}
