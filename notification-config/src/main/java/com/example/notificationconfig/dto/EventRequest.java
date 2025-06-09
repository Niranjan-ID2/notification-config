package com.example.notificationconfig.dto;

import lombok.*;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventRequest {
    String name;
    String subscriberId;
    String email;
    String phone;
    HashMap<String, Object> payload;
}
