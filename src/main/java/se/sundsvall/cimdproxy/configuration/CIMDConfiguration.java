package se.sundsvall.cimdproxy.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CIMDProperties.class)
class CIMDConfiguration {

}
