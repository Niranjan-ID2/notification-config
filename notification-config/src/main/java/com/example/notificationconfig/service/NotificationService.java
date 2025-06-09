package com.example.notificationconfig.service;

import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.dto.TriggerEventRequest;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendNotificationEvent(EventRequest eventRequest);
}
