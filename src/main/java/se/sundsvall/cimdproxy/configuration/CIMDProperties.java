package se.sundsvall.cimdproxy.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cimd")
public record CIMDProperties(

	@DefaultValue("9971") int port,

	@Valid @NotNull SSL ssl,

	@DefaultValue("true") boolean useCimdChecksum) {

	public record SSL(@DefaultValue("false") boolean enabled) {}
}
