package com.example.notificationconfig.service;

import com.example.notificationconfig.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NovuEmailNotificationService implements EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NovuEmailNotificationService.class);

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        // TODO: Implement Novu API call here. For now, logging the request.
        logger.info("Attempting to send email with Novu (mock implementation):");
        logger.info("From: {}", emailRequest.getEmail());
        if (emailRequest.getTo() != null && !emailRequest.getTo().isEmpty()) {
            logger.info("To: {}", String.join(", ", emailRequest.getTo()));
        }
        if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
            logger.info("Cc: {}", String.join(", ", emailRequest.getCc()));
        }
        if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
            logger.info("Bcc: {}", String.join(", ", emailRequest.getBcc()));
        }
        logger.info("Subject: {}", emailRequest.getSubject());
        logger.info("Body: {}", emailRequest.getBody());
        logger.info("Signature: {}", emailRequest.getSignature());
        if (emailRequest.getEmailVariables() != null && !emailRequest.getEmailVariables().isEmpty()) {
            logger.info("Email Variables: {}", emailRequest.getEmailVariables());
        }
        logger.info("Email request processed by NovuEmailNotificationService.");
    }
}
