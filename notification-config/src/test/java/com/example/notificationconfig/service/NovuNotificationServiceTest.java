package com.example.notificationconfig.service;

import com.example.notificationconfig.serviceImpl.NovuNotificationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class NovuNotificationServiceTest {

    @InjectMocks
    private NovuNotificationService novuEmailNotificationService;

}
