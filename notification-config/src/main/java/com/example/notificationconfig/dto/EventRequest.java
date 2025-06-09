package com.example.notificationconfig.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    @NotBlank(message = "Event name is required")
    String name;

    @NotBlank(message = "Subscriber ID is required")
    String subscriberId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email;

    String phone; // Optional
    HashMap<String, Object> payload; // Optional, or can have specific validation if needed
}
