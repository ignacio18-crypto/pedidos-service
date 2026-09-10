package cl.duoc.pedidosservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${cognito.app-client-id}")
    private String appClientId;

      @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CLIENTE")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/pedidos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CLIENTE")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/pedidos/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/pedidos/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> clientIdValidator =
                new JwtClaimValidator<String>("client_id", clientId -> appClientId.equals(clientId));

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> combined =
                new DelegatingOAuth2TokenValidator<>(defaultValidators, clientIdValidator);

        decoder.setJwtValidator(combined);
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> grupos = jwt.getClaimAsStringList("cognito:groups");
            if (grupos == null) {
                return Collections.emptyList();
            }
            return grupos.stream()
                    .map(grupo -> new SimpleGrantedAuthority("ROLE_" + grupo))
                    .collect(Collectors.toList());
        });
        return converter;
    }
}