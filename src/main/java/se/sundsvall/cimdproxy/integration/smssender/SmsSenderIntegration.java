package se.sundsvall.cimdproxy.integration.smssender;

import static java.lang.Boolean.TRUE;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
public class SmsSenderIntegration {

	static final String INTEGRATION_NAME = "SmsSender";

	private static final Logger LOG = LoggerFactory.getLogger(SmsSenderIntegration.class);

	private final SmsSenderIntegrationProperties properties;
	private final SmsSenderClient client;

	SmsSenderIntegration(final SmsSenderIntegrationProperties properties, final SmsSenderClient client) {
		this.properties = properties;
		this.client = client;
	}

	public boolean sendSms(final String destinationNumber, final String message) {
		try {
			var request = new SendSmsRequest()
				.priority(SendSmsRequest.PriorityEnum.HIGH)
				.sender(new Sender()
					.name(properties.sms().from()))
				.mobileNumber(destinationNumber)
				.message(message);

			var result = client.sendSms(properties.municipalityId(), request);

			if (TRUE.equals(result.getSent())) {
				LOG.info("SMS sent");
				return true;
			}

			LOG.info("Unable to send SMS");
			return false;
		} catch (Exception e) {
			LOG.warn("Unable to send SMS", e);
			return false;
		}
	}
}
