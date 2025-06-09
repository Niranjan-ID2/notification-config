package com.example.notificationconfig.service;

import com.example.notificationconfig.dto.EmailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class NovuEmailNotificationServiceTest {

    @InjectMocks
    private NovuEmailNotificationService novuEmailNotificationService;

    @Test
    void sendEmail_shouldLogRequestDetails() {
        // Setup a ListAppender to capture log output
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(NovuEmailNotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);

        EmailRequest emailRequest = new EmailRequest(
                "sender@example.com",
                Collections.singletonList("recipient@example.com"),
                Collections.singletonList("cc@example.com"),
                Collections.singletonList("bcc@example.com"),
                "Test Subject",
                "Test Body",
                "Test Signature",
                Collections.singletonMap("variableKey", "variableValue")
        );

        novuEmailNotificationService.sendEmail(emailRequest);

        // Verify log output
        List<ILoggingEvent> logsList = listAppender.list;
        // Simple check that some logging occurred, more specific checks can be added
        // For example, checking that specific parts of the emailRequest are logged.
        // assertTrue(logsList.size() > 5, "Expected several log statements");
        // Using AssertJ for more fluent assertions
        assertThat(logsList).extracting(ILoggingEvent::getFormattedMessage)
            .anySatisfy(message -> assertThat(message).contains("Attempting to send email with Novu (mock implementation):"))
            .anySatisfy(message -> assertThat(message).contains("From: sender@example.com"))
            .anySatisfy(message -> assertThat(message).contains("To: recipient@example.com"))
            .anySatisfy(message -> assertThat(message).contains("Cc: cc@example.com"))
            .anySatisfy(message -> assertThat(message).contains("Bcc: bcc@example.com"))
            .anySatisfy(message -> assertThat(message).contains("Subject: Test Subject"))
            .anySatisfy(message -> assertThat(message).contains("Body: Test Body"))
            .anySatisfy(message -> assertThat(message).contains("Signature: Test Signature"))
            .anySatisfy(message -> assertThat(message).contains("Email Variables: {variableKey=variableValue}"))
            .anySatisfy(message -> assertThat(message).contains("Email request processed by NovuEmailNotificationService."));


        // Detach appender to avoid interference with other tests
        serviceLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void sendEmail_withNullOptionalFields_shouldLogWithoutErrors() {
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(NovuEmailNotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);

        EmailRequest emailRequest = new EmailRequest(
                "sender@example.com",
                null, // To is null
                null, // Cc is null
                null, // Bcc is null
                "Minimal Subject",
                "Minimal Body",
                null, // Signature is null
                null  // Variables are null
        );

        novuEmailNotificationService.sendEmail(emailRequest);

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).extracting(ILoggingEvent::getFormattedMessage)
            .anySatisfy(message -> assertThat(message).contains("Attempting to send email with Novu (mock implementation):"))
            .anySatisfy(message -> assertThat(message).contains("From: sender@example.com"))
            .anySatisfy(message -> assertThat(message).contains("Subject: Minimal Subject"))
            .anySatisfy(message -> assertThat(message).contains("Body: Minimal Body"))
            .anySatisfy(message -> assertThat(message).doesNotContain("To:")) // Check that "To:" is not logged if list is null
            .anySatisfy(message -> assertThat(message).doesNotContain("Cc:")) // Check that "Cc:" is not logged if list is null
            .anySatisfy(message -> assertThat(message).doesNotContain("Bcc:")) // Check that "Bcc:" is not logged if list is null
            .anySatisfy(message -> assertThat(message).doesNotContain("Signature: null")) // Check signature logging
            .anySatisfy(message -> assertThat(message).doesNotContain("Email Variables:"))
            .anySatisfy(message -> assertThat(message).contains("Email request processed by NovuEmailNotificationService."));

        serviceLogger.detachAppender(listAppender);
        listAppender.stop();
    }
}
