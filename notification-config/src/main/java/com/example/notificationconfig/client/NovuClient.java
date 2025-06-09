package com.example.notificationconfig.client;

import com.example.notificationconfig.config.FeignConfig;
import com.example.notificationconfig.dto.TriggerEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NovuClient",
        url = "${novu.api-host}/v1", // Corrected to use novu.api-host and append /v1
        configuration = { FeignConfig.class })
public interface NovuClient {

    @PostMapping(
            value = "/events/trigger",
            consumes = "application/json",
            headers = "Authorization=ApiKey ${novu.secret-key}"
    )
    void triggerEvent(@RequestBody TriggerEventRequest request);
}
