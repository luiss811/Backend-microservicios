package com.microservicio.alumnos.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            template.header("Authorization", "Bearer " + jwt.getTokenValue());
        } else if (authentication.getCredentials() instanceof String token && !token.isBlank()) {
            template.header("Authorization", "Bearer " + token);
        }
    }
}
