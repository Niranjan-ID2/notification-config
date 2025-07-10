package com.notification.service;

import com.notification.dto.EmailRequest;

/**
 * Interface for services that send emails.
 * This abstraction allows for different implementations of email sending logic.
 */
public interface EmailSenderService {

    /**
     * Sends an email based on the provided request details.
     *
     * @param request The {@link EmailRequest} containing all necessary information for sending the email.
     * @throws com.notification.exception.EmailSendingException if an error occurs during email dispatch.
     */
    void sendEmail(EmailRequest request);
}
