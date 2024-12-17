package se.sundsvall.cimdproxy.integration.smssender;

import static se.sundsvall.cimdproxy.integration.smssender.SmsSenderIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.sms-sender.base-url}",
	configuration = SmsSenderIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface SmsSenderClient {

	@PostMapping("/{municipalityId}/send/sms")
	SendSmsResponse sendSms(@PathVariable("municipalityId") final String municipalityId, @RequestBody final SendSmsRequest request);
}
