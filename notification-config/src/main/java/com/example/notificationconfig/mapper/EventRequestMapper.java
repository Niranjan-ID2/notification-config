package com.example.notificationconfig.mapper;

import com.example.notificationconfig.dto.EventRequest;
import com.example.notificationconfig.dto.TriggerEventRequest;
import org.springframework.stereotype.Component;

@Component
public class EventRequestMapper {
    public TriggerEventRequest toTriggerEventRequest(EventRequest eventRequest) {
        return TriggerEventRequest.builder()
                .name(eventRequest.getName())
                .to(TriggerEventRequest.To.builder()
                        .email(eventRequest.getEmail())
                        .phone(eventRequest.getPhone())
                        .subscriberId(eventRequest.getSubscriberId())
                        .build())
                .payload(eventRequest.getPayload())
                .build();
    }
}
