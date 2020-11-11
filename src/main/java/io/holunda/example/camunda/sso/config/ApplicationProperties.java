package io.holunda.example.camunda.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "application")
@ConstructorBinding
public class ApplicationProperties {
    private final String webAppRole;
    private final String registration;

    public ApplicationProperties(
        String webAppRole,
        String registration
    ) {
        this.registration = registration;
        this.webAppRole = webAppRole;
    }

    public String getWebAppRole() {
        return webAppRole;
    }

    public String getRegistration() {
        return registration;
    }
}
