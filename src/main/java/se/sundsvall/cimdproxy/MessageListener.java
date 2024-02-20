package se.sundsvall.cimdproxy;

import org.springframework.stereotype.Component;

import se.sundsvall.cimdproxy.cimd.CIMD;
import se.sundsvall.cimdproxy.cimd.CIMDMessage;
import se.sundsvall.cimdproxy.cimd.CIMDMessageListener;
import se.sundsvall.cimdproxy.integration.smssender.SmsSenderIntegration;

@Component
class MessageListener implements CIMDMessageListener {

    private final SmsSenderIntegration smsSenderIntegration;

    MessageListener(final CIMD cimd, final SmsSenderIntegration smsSenderIntegration) {
        this.smsSenderIntegration = smsSenderIntegration;

        cimd.start(this);
    }

    @Override
    public boolean handleMessage(final CIMDMessage message) {
        return smsSenderIntegration.sendSms(message.destinationAddress(), message.content());
    }
}
