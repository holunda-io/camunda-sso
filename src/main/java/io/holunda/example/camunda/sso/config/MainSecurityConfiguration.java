package io.holunda.example.camunda.sso.config;

import io.holunda.example.camunda.sso.config.spring.GrantedAuthoritiesExtractor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static io.holunda.example.camunda.sso.rest.REST.REST_PREFIX;

/**
 * Enables security by OIDC JWT Token.
 */
@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
public class MainSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] UNPROTECTED_PATHS = {
        "/actuator/**", // spring actuator endpoints
        "/error", // spring error page
        "/public", // visible resources, for example for SPA
    };

    private static final String CAMUNDA_JERSEY_PATH = "/rest/**";

    private final GrantedAuthoritiesExtractor grantedAuthoritiesExtractor;

    public MainSecurityConfiguration(GrantedAuthoritiesExtractor grantedAuthoritiesExtractor) {
        this.grantedAuthoritiesExtractor = grantedAuthoritiesExtractor;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
          .csrf()
            .ignoringAntMatchers(UNPROTECTED_PATHS)
            .and()
          .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll() // allow options
            .antMatchers(UNPROTECTED_PATHS).permitAll() // permit access to public resources
            .antMatchers(REST_PREFIX + "/**").authenticated() // require authentication for any REST controller. (configure differently here)
            .antMatchers(CAMUNDA_JERSEY_PATH).authenticated() // require authentication for camunda REST endpoints. (configure differently here)
          .and()
          .oauth2ResourceServer()
            .jwt()
              .jwtAuthenticationConverter(grantedAuthoritiesExtractor);
        // @formatter:on
    }

}
