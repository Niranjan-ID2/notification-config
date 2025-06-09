package com.example.notificationconfig.controller;

import com.example.notificationconfig.dto.EmailRequest;
import com.example.notificationconfig.service.EmailNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailNotificationService emailNotificationService;

    @Autowired
    public EmailController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerEmail(@Valid @RequestBody EmailRequest emailRequest) {
        emailNotificationService.sendEmail(emailRequest);
        return ResponseEntity.ok("Email trigger request received successfully.");
    }
}
