package com.notification.exception;

/**
 * Custom exception thrown when an error occurs while attempting to send an email.
 * This could be due to issues with the email provider, invalid recipient addresses,
 * or other related problems.
 */
public class EmailSendingException extends RuntimeException {

    /**
     * Constructs a new EmailSendingException with the specified detail message.
     *
     * @param message The detail message.
     */
    public EmailSendingException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmailSendingException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause of the exception.
     */
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
