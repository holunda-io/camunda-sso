package io.holunda.example.camunda.sso.config;

import io.holunda.example.camunda.sso.config.camunda.OAuthContainerBasedAuthenticationProvider;
import io.holunda.example.camunda.sso.config.camunda.RestExceptionHandler;
import io.holunda.example.camunda.sso.config.spring.GrantedAuthoritiesExtractor;
import io.holunda.example.camunda.sso.config.spring.TokenParsingOAuth2UserService;
import io.holunda.example.camunda.sso.config.spring.TokenParsingOidcUserService;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.annotation.PostConstruct;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.singletonMap;

/**
 * A separate {@code WebSecurityConfigurerAdapter} that applies only to the camunda webapps. It configures the SSO role required to access the webapps,
 * integrates the webapp security with spring security and adds an OAuth2 login so that unauthenticated users are redirected to the SSO login page.
 * This works together with the {@code spring.security.oauth2.client.registration.*}
 * and {@code spring.security.oauth2.client.provider.*} configuration properties.
 */
@Configuration
@Order(90)
public class CamundaWebAppsSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // The paths used by camunda webapps. These are the paths that our HttpSecurity applies to
    private static final String[] CAMUNDA_APP_PATHS = { "/app/**", "/api/**", "/lib/**" };

    private final TokenParsingOAuth2UserService oAuth2UserService;
    private final ApplicationProperties applicationProperties;

    public CamundaWebAppsSecurityConfiguration(
        GrantedAuthoritiesExtractor grantedAuthoritiesExtractor,
        ApplicationProperties applicationProperties
    ) {
        this.oAuth2UserService = new TokenParsingOAuth2UserService(grantedAuthoritiesExtractor);
        this.applicationProperties = applicationProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // @formatter:off
        http
            // Only apply this HttpSecurity to the camunda webapp paths
            .requestMatchers().antMatchers(CAMUNDA_APP_PATHS).and()
                // Disable CSRF for these paths
                .csrf().disable()
                // Any requests on these paths require the configured role
                .authorizeRequests()
                    .anyRequest()
                    .hasRole(applicationProperties.getWebAppRole())
                    .and()
                // Redirect user to SSO login if not yet authenticated
                .oauth2Login()
                    .authorizationEndpoint()
                        // put the authorization endpoint under the /app/ prefix so that it is covered by this HttpSecurity
                        .baseUri("/app" + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI)
                        .and()
                    .userInfoEndpoint()
                        // Configure specialized user services - see javadoc of TokenParsingOAuth2UserService for an explanation
                        .userService(oAuth2UserService)
                        .oidcUserService(new TokenParsingOidcUserService(oAuth2UserService))
                        .and()
                    // put the login processing endpoint under the /app/ prefix so that is is covered by this HttpSecurity. If you change this, remember to also change `spring.security.oauth2.client.registration.my-client-registration.redirect-uri`.
                    .loginProcessingUrl("/app" + OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI)
                    // Set the authorization endpoint for the my-client-registration clientRegistration as the login page because that's the only one we want to use.
                    .loginPage("/app" + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + applicationProperties.getRegistration());
        // @formatter:on
    }


    // The ForwardedHeaderFilter is required to correctly assemble the redirect URL for OAUth2 login. Without the filter, Spring generates an http URL even though the OpenShift
    // route is accessed through https.
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    // This filter is responsible for integrating the camunda webapps security with spring security. It is configured with the ContainerBasedAuthenticationProvider defined below.
    @Bean
    public FilterRegistrationBean<ContainerBasedAuthenticationFilter> containerBasedAuthenticationFilterRegistrationBean() {
        FilterRegistrationBean<ContainerBasedAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(new ContainerBasedAuthenticationFilter());
        registrationBean.setInitParameters(singletonMap(ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM, OAuthContainerBasedAuthenticationProvider.class.getName()));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        return registrationBean;
    }

    // Quite a dirty hack to replace camunda's RestExceptionHandler with our own that logs exceptions more selectively.
    // Workaround for https://app.camunda.com/jira/browse/CAM-10799.
    @PostConstruct
    public void replaceRestExceptionHandler() {
        Set<Class<?>> configurationClasses = CamundaRestResources.getConfigurationClasses();
        configurationClasses.remove(org.camunda.bpm.engine.rest.exception.RestExceptionHandler.class);
        configurationClasses.add(RestExceptionHandler.class);
    }
}
