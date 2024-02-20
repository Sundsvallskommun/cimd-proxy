package se.sundsvall.cimdproxy.integration.smssender;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

import feign.RequestLine;
import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;

@FeignClient(
    name = SmsSenderIntegration.INTEGRATION_NAME,
    url = "${integration.sms-sender.base-url}",
    configuration = SmsSenderIntegrationConfiguration.class
)
interface SmsSenderClient {

    @RequestLine("POST /send/sms?flash=true")
    SendSmsResponse sendSms(@RequestBody final SendSmsRequest request);
}
