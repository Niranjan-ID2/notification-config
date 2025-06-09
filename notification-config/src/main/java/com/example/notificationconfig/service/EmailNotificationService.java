package com.example.notificationconfig.service;

import com.example.notificationconfig.dto.EmailRequest;

public interface EmailNotificationService {
    void sendEmail(EmailRequest emailRequest);
}
