package com.example_project_name.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Rules shared by every security profile, so the waffle and kerberos chains only differ in how a
 * user is authenticated - never in what they may access.
 */
public final class SecurityRules {

    private SecurityRules() {
    }

    public static HttpSecurity apply(HttpSecurity http) throws Exception {
        return http
            // Negotiate runs per request, like the original JWT setup: no server-side session to replicate.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Negotiate credentials are sent by the browser automatically (like cookies), so CSRF protection
            // stays ON. spa(): token in the XSRF-TOKEN cookie, the frontend echoes it in X-XSRF-TOKEN.
            .csrf(csrf -> csrf.spa())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            // Define authorization rules for endpoints and roles here. Adjust as needed.
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/public/**",
                "/error",
                "/swagger-ui/**", //swagger endpoints public for testing - consider securing or removing in production
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/test",
                "/v3/api-docs.yaml",
                // actuator lives on the management port (8081), which is not exposed through the frontend proxy
                "/actuator/health/**",
                "/actuator/info",
                "/actuator/prometheus").permitAll() // open endpoints
                .requestMatchers("/secured/admin").hasRole("ADMIN") // roles come from app.security.role-mappings
                .requestMatchers("/secured/user").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            // default: plain 401; the SSO profiles replace this with a "WWW-Authenticate: Negotiate" entry point
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    }
}
