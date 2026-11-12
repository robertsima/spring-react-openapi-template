package com.example_project_name.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Windows SSO with Spring Security Kerberos (profile "kerberos").
 *
 * Validates the browser's SPNEGO/Kerberos ticket against a service keytab, so it runs anywhere,
 * including the Linux container. Your AD admin needs to:
 *   1. create a service account and register the SPN:  setspn -S HTTP/app.mydomain.com svc-app
 *   2. export a keytab:  ktpass /princ HTTP/app.mydomain.com@MYDOMAIN.COM /mapuser svc-app /pass * /crypto AES256-SHA1 /ptype KRB5_NT_PRINCIPAL /out app.keytab
 * The SPN host must be the host name the BROWSER uses (the frontend), and that site must be in the
 * browser's Local Intranet zone / auth-server allowlist, or the browser won't offer Kerberos.
 */
@Configuration
@Profile("kerberos")
public class KerberosSecurityConfig {

    @Bean
    public SunJaasKerberosTicketValidator kerberosTicketValidator(AppSecurityProperties properties) {
        AppSecurityProperties.Kerberos kerberos = properties.kerberos();
        SunJaasKerberosTicketValidator validator = new SunJaasKerberosTicketValidator();
        validator.setServicePrincipal(kerberos.servicePrincipal());
        validator.setKeyTabLocation(new FileSystemResource(kerberos.keytabLocation()));
        validator.setDebug(kerberos.debug());
        return validator;
    }

    /**
     * A Kerberos ticket only proves WHO the user is (e.g. jdoe@MYDOMAIN.COM); it carries no groups we can read.
     * Here roles come from mapping the principal name. To map AD groups instead, look them up via LDAP
     * (spring-security-ldap, LdapUserDetailsService / memberOf) and pass them to roleMapper.authoritiesFor(...).
     */
    @Bean
    public UserDetailsService kerberosUserDetailsService(RoleMapper roleMapper) {
        return username -> User.withUsername(username)
                .password("N/A") // never used - the ticket is the credential
                .authorities(roleMapper.authoritiesFor(List.of(username)))
                .build();
    }

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider(
            SunJaasKerberosTicketValidator ticketValidator, UserDetailsService kerberosUserDetailsService) {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(ticketValidator);
        provider.setUserDetailsService(kerberosUserDetailsService);
        return provider;
    }

    @Bean
    public SecurityFilterChain kerberosSecurityFilterChain(HttpSecurity http,
            KerberosServiceAuthenticationProvider kerberosProvider) throws Exception {

        SpnegoAuthenticationProcessingFilter spnegoFilter = new SpnegoAuthenticationProcessingFilter();
        spnegoFilter.setAuthenticationManager(new ProviderManager(kerberosProvider));

        SecurityRules.apply(http)
            .authenticationProvider(kerberosProvider)
            .addFilterBefore(spnegoFilter, BasicAuthenticationFilter.class)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new SpnegoEntryPoint()));

        return http.build();
    }
}
