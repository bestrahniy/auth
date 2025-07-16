package com.auth.config;

import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import lombok.AllArgsConstructor;

/**
 * custom filter class
 */
@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAunteficationProvider customAunteficationProvider;

    /**
     * customized entity from claims: add ROLE_ with it
     * @return customized JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();
        rolesConverter.setAuthorityPrefix("ROLE_");
        rolesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(rolesConverter);
        return authConverter;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * configurate if security filter chain
     * @param httpSecurity builder for customize request
     * @param jwtAuthenticationConverter converter jwt token
     * @param jwtDecoder dekoder jwt token
     * @return customized http request
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity httpSecurity,
        JwtAuthenticationConverter jwtAuthenticationConverter,
        JwtDecoder jwtDecoder) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            request -> request
            .requestMatchers(
                "/auth/signup/**", "/auth/signin/**"
            ).permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/user-roles/save")
                .hasAnyRole("ADMIN")
            .anyRequest().permitAll()
        )
        .authenticationProvider(customAunteficationProvider)
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
            )
        );

        return httpSecurity.build();
    }

    /**
     * creating jwt decoder
     * @param secretKeyHex secret key
     * @return jwt decoder
     * @throws DecoderException
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "security.jwt.secret_key")
    public JwtDecoder hmacJwtDecoder(
        @Value("${security.jwt.secret_key}") String secretKeyHex
    ) throws DecoderException {
        byte[] keyBytes = org.apache.commons.codec.binary.Hex.decodeHex(secretKeyHex.toCharArray());
        javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    /**
     * creating jwt decoder based on jwk set uri
     * @param jwkUri uri for getting jwk set
     * @return decoder jwt
     */
    @Bean
    @ConditionalOnProperty(name = "security.oauth2.resourceserver.jwt.jwk-set-uri")
    public JwtDecoder jwkSetUriProperty(@Value("${security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUri) {
        return NimbusJwtDecoder.withJwkSetUri(jwkUri).build();
    }

}
