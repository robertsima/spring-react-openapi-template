package com.example_project_name.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import waffle.servlet.spi.NegotiateSecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProvider;
import waffle.servlet.spi.SecurityFilterProviderCollection;
import waffle.spring.GrantedAuthorityFactory;
import waffle.spring.NegotiateSecurityFilter;
import waffle.spring.NegotiateSecurityFilterEntryPoint;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

/**
 * Windows SSO with Waffle (profile "waffle").
 *
 * Waffle validates Negotiate tokens with the Windows SSPI APIs, so the backend MUST run on a domain-joined
 * Windows host (or Windows container) - it cannot work in the Linux image. For Linux use the "kerberos" profile.
 *
 * Only the Negotiate protocol is offered (Kerberos, falling back to NTLM inside SPNEGO). Plain NTLM is
 * connection-oriented and does not survive the nginx reverse proxy in front of the API.
 */
@Configuration
@Profile("waffle")
public class WaffleSecurityConfig {

    @Bean
    public IWindowsAuthProvider waffleWindowsAuthProvider() {
        return new WindowsAuthProviderImpl();
    }

    @Bean
    public SecurityFilterProviderCollection waffleSecurityFilterProviders(IWindowsAuthProvider windowsAuthProvider) {
        NegotiateSecurityFilterProvider negotiate = new NegotiateSecurityFilterProvider(windowsAuthProvider);
        negotiate.setProtocols(List.of("Negotiate"));
        return new SecurityFilterProviderCollection(new SecurityFilterProvider[] { negotiate });
    }

    @Bean
    public SecurityFilterChain waffleSecurityFilterChain(HttpSecurity http,
            SecurityFilterProviderCollection providers, RoleMapper roleMapper,
            AppSecurityProperties properties) throws Exception {

        NegotiateSecurityFilterEntryPoint entryPoint = new NegotiateSecurityFilterEntryPoint();
        entryPoint.setProvider(providers);

        // Created here rather than as a @Bean so Spring Boot doesn't also register it as a plain servlet filter.
        NegotiateSecurityFilter negotiateFilter = new NegotiateSecurityFilter();
        negotiateFilter.setProvider(providers);
        negotiateFilter.setAllowGuestLogin(false); // never let the Windows Guest account in
        negotiateFilter.setDefaultGrantedAuthority(new SimpleGrantedAuthority(
                RoleMapper.ROLE_PREFIX + properties.defaultRole().toUpperCase(java.util.Locale.ROOT)));
        negotiateFilter.setGrantedAuthorityFactory(groupAuthorities(roleMapper));

        SecurityRules.apply(http)
            .addFilterBefore(negotiateFilter, BasicAuthenticationFilter.class)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));

        return http.build();
    }

    // Waffle calls this once per AD group of the user: mapped groups become ROLE_<role>,
    // unmapped ones are kept as GROUP_<DOMAIN\Group> (useful for @PreAuthorize, never matched by hasRole).
    private GrantedAuthorityFactory groupAuthorities(RoleMapper roleMapper) {
        return account -> {
            String role = roleMapper.roleFor(account.getFqn());
            return new SimpleGrantedAuthority(role != null
                    ? RoleMapper.ROLE_PREFIX + role
                    : "GROUP_" + account.getFqn());
        };
    }
}
