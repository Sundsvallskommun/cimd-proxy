package se.sundsvall.cimdproxy.integration.smssender;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.sms-sender")
record SmsSenderIntegrationProperties(

        @NotBlank
        String baseUrl,

        @NotBlank
        String municipalityId,

        @Valid
        OAuth2 oAuth2,

        @Valid
        Sms sms,

        @DefaultValue("PT15S")
        Duration readTimeout,

        @DefaultValue("PT5S")
        Duration connectTimeout) {

    record Sms(

        @NotBlank
        @DefaultValue("Sundsvall")
        String from) { }

    record OAuth2(

        @NotBlank
        String tokenUrl,

        @NotBlank
        String clientId,

        @NotBlank
        String clientSecret,

        @DefaultValue("client_credentials")
        String grantType) { }
}
