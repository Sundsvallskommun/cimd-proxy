package se.sundsvall.cimdproxy.configuration;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

@ConfigurationProperties(prefix = "cimd")
public record CIMDProperties(

        @DefaultValue("9971")
        int port,

        SSL ssl,

        @DefaultValue("true")
        boolean useCimdChecksum) {

    public record SSL(

            @DefaultValue("false")
            boolean enabled,

            Keystore keystore) {

        public record Keystore(
            @NotBlank
            String alias,

            @ValidBase64
            String data,

            String password) { }
    }
}
