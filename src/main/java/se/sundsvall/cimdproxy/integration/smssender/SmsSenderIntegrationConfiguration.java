package se.sundsvall.cimdproxy.integration.smssender;

import feign.Request;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
class SmsSenderIntegrationConfiguration {

	private final SmsSenderIntegrationProperties properties;

	SmsSenderIntegrationConfiguration(final SmsSenderIntegrationProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer customizer() {
		return FeignMultiCustomizer.create()
			.withRequestInterceptor(template -> template.query("flash", "true"))
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration
				.withRegistrationId(SmsSenderIntegration.INTEGRATION_NAME)
				.tokenUri(properties.oAuth2().tokenUrl())
				.clientId(properties.oAuth2().clientId())
				.clientSecret(properties.oAuth2().clientSecret())
				.authorizationGrantType(new AuthorizationGrantType(properties.oAuth2().grantType()))
				.build())
			.withErrorDecoder(new ProblemErrorDecoder(SmsSenderIntegration.INTEGRATION_NAME))
			.withRequestOptions(new Request.Options(
				properties.connectTimeout().toMillis(), TimeUnit.MILLISECONDS,
				properties.readTimeout().toMillis(), TimeUnit.MILLISECONDS,
				true))
			.composeCustomizersToOne();
	}
}
