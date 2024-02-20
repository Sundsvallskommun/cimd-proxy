package se.sundsvall.cimdproxy.integration.smssender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.Sender;

@Component
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
public class SmsSenderIntegration {

    static final String INTEGRATION_NAME = "SmsSender";

    private static final Logger LOG = LoggerFactory.getLogger(SmsSenderIntegration.class);

    private final SmsSenderIntegrationProperties properties;
    private final SmsSenderClient client;

    public SmsSenderIntegration(final SmsSenderIntegrationProperties properties, final SmsSenderClient client) {
        this.properties = properties;
        this.client = client;
    }

    public boolean sendSms(final String destinationNumber, final String message) {
        try {
            var request = new SendSmsRequest()
                .sender(new Sender()
                    .name(properties.sms().from()))
                .mobileNumber(destinationNumber)
                .message(message);

            var result = client.sendSms(request);

            if (Boolean.TRUE.equals(result.getSent())) {
                LOG.info("SMS sent");
            } else {
                LOG.info("Unable to send SMS");
            }
            return result.getSent();
        } catch (Exception e) {
            LOG.warn("Unable to send SMS", e);

            return false;
        }
    }
}
