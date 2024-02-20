package se.sundsvall.cimdproxy.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;

@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTests {

    @Mock
    private SmsSenderIntegrationProperties.Sms mockSms;
    @Mock
    private SmsSenderClient mockSmsSenderClient;

    private SmsSenderIntegration smsSenderIntegration;

    @BeforeEach
    void setUp() {
        when(mockSms.from()).thenReturn("SomeSender");

        var smsSenderIntegrationProperties = new SmsSenderIntegrationProperties(null, null, mockSms, null, null);

        smsSenderIntegration = new SmsSenderIntegration(smsSenderIntegrationProperties,
            mockSmsSenderClient);
    }

    @Test
    void testSendSmsSuccess() {
        when(mockSmsSenderClient.sendSms(any(SendSmsRequest.class)))
            .thenReturn(new SendSmsResponse().sent(true));

        var result = smsSenderIntegration.sendSms("0701234567", "someMessage");
        assertThat(result).isTrue();

        verify(mockSms, times(1)).from();
        verify(mockSmsSenderClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    void testSendSmsFailure() {
        when(mockSmsSenderClient.sendSms(any(SendSmsRequest.class)))
            .thenReturn(new SendSmsResponse().sent(false));

        var result = smsSenderIntegration.sendSms("0701234567", "someMessage");
        assertThat(result).isFalse();

        verify(mockSms, times(1)).from();
        verify(mockSmsSenderClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    void testSendSmsWhenClientThrowsException() {
        when(mockSmsSenderClient.sendSms(any(SendSmsRequest.class)))
            .thenThrow(new RuntimeException("dummy"));

        var result = smsSenderIntegration.sendSms("0701234567", "someMessage");
        assertThat(result).isFalse();

        verify(mockSms, times(1)).from();
        verify(mockSmsSenderClient, times(1)).sendSms(any(SendSmsRequest.class));
    }
}
