package com.example_project_name.security;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * LOCAL DEVELOPMENT ONLY (profile "localdev"): every request is signed in as a fixed fake Windows user,
 * so you can run the stack without an AD domain. Roles still go through RoleMapper, so you can test
 * role mappings by listing groups:
 *
 *   SPRING_PROFILES_ACTIVE=sqlserver,localdev
 *   (user and groups: app.security.dev-user in application-localdev.yml)
 *
 * Refuses to start next to a real SSO profile.
 */
@Slf4j
@Configuration
@Profile("localdev")
public class LocalDevSecurityConfig {

    @Bean
    public SecurityFilterChain localDevSecurityFilterChain(HttpSecurity http, RoleMapper roleMapper, Environment env,
            AppSecurityProperties properties) throws Exception {
        String username = properties.devUser().name();
        List<String> groups = properties.devUser().groups();

        if (env.matchesProfiles("waffle | kerberos")) {
            throw new IllegalStateException("'localdev' must not be combined with 'waffle' or 'kerberos'");
        }
        log.warn("LOCALDEV auth: every request is authenticated as {} {} - never use this profile outside development",
                username, groups);

        List<String> identities = new java.util.ArrayList<>(groups);
        identities.add(username);
        var authorities = roleMapper.authoritiesFor(identities);

        OncePerRequestFilter fakeSso = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(username, null, authorities));
                chain.doFilter(request, response);
            }
        };

        return SecurityRules.apply(http)
            .addFilterBefore(fakeSso, BasicAuthenticationFilter.class)
            .build();
    }
}
