package io.holunda.example.camunda.sso.config.spring;

import io.holunda.example.camunda.sso.config.spring.GrantedAuthoritiesExtractor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@code OAuth2UserService} that does not call the UserInfo endpoint but just parses the access token. This is probably not really the intention of
 * the {@code OAuth2UserService} but it works with keycloak while the standard solution doesn't. Keycloak doesn't seem to return the user's roles from the UserInfo endpoint
 * so we have no way of determining the user's roles from the result of the standard {@code OAuth2UserService} even if we use a
 * {@link org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper GrantedAuthoritiesMapper} as seems to be the usually preferred way.
 *
 * See also <a href="https://docs.spring.io/spring-security/site/docs/5.1.4.RELEASE/reference/htmlsingle/#oauth2login-advanced-userinfo-endpoint">UserInfo Endpoint in Spring Security documentation</a>.
 */
public class TokenParsingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final GrantedAuthoritiesExtractor grantedAuthoritiesExtractor;
    private final ConcurrentHashMap<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    public TokenParsingOAuth2UserService(GrantedAuthoritiesExtractor grantedAuthoritiesExtractor) {
        this.grantedAuthoritiesExtractor = grantedAuthoritiesExtractor;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        JwtDecoder jwtDecoder = jwtDecoders.computeIfAbsent(
            clientRegistration.getRegistrationId(),
            ignored -> NimbusJwtDecoder.withJwkSetUri(clientRegistration.getProviderDetails().getJwkSetUri()).build()
        );
        Jwt jwt = jwtDecoder.decode(userRequest.getAccessToken().getTokenValue());
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) grantedAuthoritiesExtractor.convert(jwt);

        return new DefaultOAuth2User(
            authenticationToken.getAuthorities(),
            authenticationToken.getTokenAttributes(),
            clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}