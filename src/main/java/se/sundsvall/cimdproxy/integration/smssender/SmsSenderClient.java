package se.sundsvall.cimdproxy.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	name = SmsSenderIntegration.INTEGRATION_NAME,
	url = "${integration.sms-sender.base-url}",
	configuration = SmsSenderIntegrationConfiguration.class)
interface SmsSenderClient {

	@PostMapping("/{municipalityId}/send/sms")
	SendSmsResponse sendSms(@PathVariable("municipalityId") final String municipalityId, @RequestBody final SendSmsRequest request);
}
