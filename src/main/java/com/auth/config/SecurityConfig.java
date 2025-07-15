package com.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
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
        .authenticationProvider(customAunteficationProvider);

        return httpSecurity.build();
    }

}
