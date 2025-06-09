package com.example.notificationconfig.client;

import com.example.notificationconfig.config.FeignConfig;
import com.example.notificationconfig.dto.TriggerEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FeignClient(name = "NovuClient",
        url = "https://api.novu.co/v1",
        configuration = { FeignConfig.class })
public interface NovuClient {

    @PostMapping(
            value = "/events/trigger",
            consumes = "application/json",
            headers = "Authorization=ApiKey ${novu.secret-key}"
    )
    void triggerEvent(@RequestBody TriggerEventRequest request);
}
