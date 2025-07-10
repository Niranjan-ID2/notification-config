package com.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Example: use an application-test.yml if needed
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context can start successfully.
        // If this test passes, it means all Spring configurations are correctly set up,
        // beans can be instantiated and injected, etc.
    }
}
