package com.example.notificationconfig.dto;

import lombok.Builder;
import java.util.Map;

@Builder
public record TriggerEventRequest(
        String name,
        To to,
        Map<String, Object> payload
) {
    @Builder
    public record To(String subscriberId, String email, String phone) {}
}
