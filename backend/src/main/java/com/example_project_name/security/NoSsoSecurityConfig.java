package com.example_project_name.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

/**
 * Fail-closed fallback when no auth profile (waffle, kerberos, localdev) is active (also what tests run with): same authorization
 * rules, but no way to authenticate, so protected endpoints return 401. Tests authenticate with @WithMockUser.
 */
@Slf4j
@Configuration
@Profile("!waffle & !kerberos & !localdev")
public class NoSsoSecurityConfig {

    @Bean
    public SecurityFilterChain noSsoSecurityFilterChain(HttpSecurity http) throws Exception {
        return SecurityRules.apply(http).build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warn() {
        log.warn("No SSO profile active (waffle | kerberos): all protected endpoints will return 401");
    }
}
