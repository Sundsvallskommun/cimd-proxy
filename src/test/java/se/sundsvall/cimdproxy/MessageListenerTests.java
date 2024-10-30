package se.sundsvall.cimdproxy;

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

import se.sundsvall.cimdproxy.cimd.CIMD;
import se.sundsvall.cimdproxy.cimd.CIMDMessage;
import se.sundsvall.cimdproxy.cimd.CIMDMessageListener;
import se.sundsvall.cimdproxy.integration.smssender.SmsSenderIntegration;

@ExtendWith(MockitoExtension.class)
class MessageListenerTests {

	@Mock
	private CIMD mockCIMD;
	@Mock
	private SmsSenderIntegration mockSmsSenderIntegration;

	private MessageListener messageListener;

	@BeforeEach
	void setUp() {
		messageListener = new MessageListener(mockCIMD, mockSmsSenderIntegration);

		verify(mockCIMD, times(1)).start(any(CIMDMessageListener.class));
	}

	@Test
	void testHandleMessageWhenSmsSenderIntegrationSucceeds() {
		when(mockSmsSenderIntegration.sendSms(any(String.class), any(String.class)))
			.thenReturn(true);

		var result = messageListener.handleMessage(new CIMDMessage("", ""));
		assertThat(result).isTrue();

		verify(mockSmsSenderIntegration, times(1)).sendSms(any(String.class), any(String.class));
	}

	@Test
	void testHandleMessageWhenSmsSenderIntegrationFails() {
		when(mockSmsSenderIntegration.sendSms(any(String.class), any(String.class)))
			.thenReturn(false);

		var result = messageListener.handleMessage(new CIMDMessage("", ""));
		assertThat(result).isFalse();

		verify(mockSmsSenderIntegration, times(1)).sendSms(any(String.class), any(String.class));
	}
}
