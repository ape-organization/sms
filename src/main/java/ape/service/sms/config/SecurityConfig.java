package ape.service.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration so the contract-first SMS API and dev tooling
 * (Swagger UI, H2 console) are reachable without the auto-generated Spring Security
 * password. Tighten this (e.g. with JWT) before exposing the service publicly.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return http.build();
    }
}
