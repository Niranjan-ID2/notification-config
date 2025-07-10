package com.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * Represents the request payload for sending an email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    /**
     * The primary recipient's email address. This field is mandatory.
     */
    @NotEmpty(message = "To email address cannot be empty.")
    @Email(message = "Invalid 'to' email address format.")
    private String to;

    /**
     * A list of CC recipients' email addresses. Optional.
     */
    private List<@Email(message = "Invalid 'cc' email address format.") String> cc;

    /**
     * A list of BCC recipients' email addresses. Optional.
     */
    private List<@Email(message = "Invalid 'bcc' email address format.") String> bcc;

    /**
     * The subject of the email. Optional.
     */
    private String subject;

    /**
     * The main content/body of the email. Optional.
     */
    private String body;

    /**
     * The signature to be appended to the email. Optional.
     */
    private String signature;

    /**
     * A map of variables that can be used for template substitution in the email body or subject. Optional.
     * For example, {"userName": "John Doe", "orderNumber": "12345"}
     */
    private Map<String, Object> emailVariables;
}
