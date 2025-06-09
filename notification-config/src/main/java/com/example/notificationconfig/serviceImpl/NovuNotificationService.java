package com.example.notificationconfig.serviceImpl;

import com.example.notificationconfig.client.NovuClient;
import com.example.notificationconfig.config.FeignConfig;
import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.dto.TriggerEventRequest;
import com.example.notificationconfig.mapper.EventRequestMapper;
import com.example.notificationconfig.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class NovuNotificationService implements NotificationService {

    private final NovuClient novuClient;
    EventRequestMapper eventRequestMapper = new EventRequestMapper();

    @Autowired
    public NovuNotificationService(NovuClient novuClient) {
        this.novuClient = novuClient;
    }

    @Override
    public void sendNotificationEvent(EventRequest eventRequest) {
        novuClient.triggerEvent(eventRequestMapper.toTriggerEventRequest(eventRequest));
    }
}
