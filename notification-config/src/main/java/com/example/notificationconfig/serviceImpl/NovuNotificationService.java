package com.example.notificationconfig.serviceImpl;

import com.example.notificationconfig.client.NovuClient;
import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.mapper.EventRequestMapper;
import com.example.notificationconfig.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NovuNotificationService implements NotificationService {

    private final NovuClient novuClient;
    private final EventRequestMapper eventRequestMapper;

    @Autowired
    public NovuNotificationService(NovuClient novuClient, EventRequestMapper eventRequestMapper) {
        this.novuClient = novuClient;
        this.eventRequestMapper = eventRequestMapper;
    }

    @Override
    public void sendNotificationEvent(EventRequest eventRequest) {
        novuClient.triggerEvent(eventRequestMapper.toTriggerEventRequest(eventRequest));
    }
}
