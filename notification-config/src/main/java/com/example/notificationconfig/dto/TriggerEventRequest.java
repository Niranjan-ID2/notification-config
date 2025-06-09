package com.example.notificationconfig.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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
