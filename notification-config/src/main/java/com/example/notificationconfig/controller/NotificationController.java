package com.example.notificationconfig.controller;

import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.dto.TriggerEventRequest;
import com.example.notificationconfig.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notify")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerNotificationEvent(@RequestBody EventRequest eventRequest) {
        notificationService.sendNotificationEvent(eventRequest);
        return ResponseEntity.ok("Notification Triggered");
    }
}
