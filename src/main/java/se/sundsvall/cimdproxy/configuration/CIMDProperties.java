package se.sundsvall.cimdproxy.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

@Validated
@ConfigurationProperties(prefix = "cimd")
public record CIMDProperties(

	@DefaultValue("9971") int port,

	@Valid @NotNull SSL ssl,

	@DefaultValue("true") boolean useCimdChecksum) {

	public record SSL(
		@DefaultValue("false") boolean enabled,

		@ValidBase64(nullable = true) String serverCert,
		@ValidBase64(nullable = true) String serverKey,
		String serverKeyPassword,
		@ValidBase64(nullable = true) String trustedCert) {
	}
}
