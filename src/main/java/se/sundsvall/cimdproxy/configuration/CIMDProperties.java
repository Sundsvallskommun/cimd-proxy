package se.sundsvall.cimdproxy.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cimd")
public record CIMDProperties(

	@DefaultValue("9971") int port,

	@DefaultValue("true") boolean useCimdChecksum) {}
