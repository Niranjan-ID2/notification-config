package com.notification.config;

import co.novu.sdk.Novu;
import co.novu.sdk.NovuConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class NovuConfig {

    private static final Logger logger = LoggerFactory.getLogger(NovuConfig.class);

    @Value("${novu.api.key}")
    private String novuApiKey;

    // Optional: Configure backend URL if needed, though SDK usually has a default
    // @Value("${novu.backend.url:https://api.novu.co}")
    // private String novuBackendUrl;

    @Bean
    public co.novu.sdk.NovuConfig novuSdkConfig() { // Renamed for clarity, returns the SDK's config object
        if (novuApiKey == null || novuApiKey.isEmpty() || "YOUR_NOVU_API_KEY".equals(novuApiKey)) {
            logger.warn("Novu API key is not configured or is using the default placeholder. Novu service might not be fully functional.");
        }
        // This bean provides the NovuConfig object, which can be injected elsewhere (like the service)
        // if direct access to API key or other config details is needed from it.
        return new co.novu.sdk.NovuConfig(novuApiKey);
    }

    @Bean
    public Novu novu(co.novu.sdk.NovuConfig novuSdkConfig) { // Takes the NovuConfig bean as a parameter
        // Configuration based on Novu SDK v1.6.0 (co.novu:novu-java)
        // The Novu client is instantiated with a NovuConfig object.

        // if (novuBackendUrl != null && !novuBackendUrl.isEmpty()) {
        //    novuSdkConfig.setBackendUrl(novuBackendUrl); // Example if backend URL needs to be set
        // }

        logger.info("Configuring Novu client with SDK co.novu:novu-java.");
        return new Novu(novuSdkConfig);
    }
}
