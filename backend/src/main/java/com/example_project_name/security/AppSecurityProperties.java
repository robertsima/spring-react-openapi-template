package com.example_project_name.security;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.security.* settings shared by both Windows SSO profiles.
 *
 * <pre>
 * app:
 *   security:
 *     default-role: USER
 *     # AD group or account -> application role. Keys contain '\' so use bracket notation.
 *     role-mappings:
 *       "[MYDOMAIN\App-Admins]": ADMIN
 *       "[jdoe@MYDOMAIN.COM]": ADMIN
 *     kerberos:
 *       service-principal: HTTP/app.mydomain.com@MYDOMAIN.COM
 *       keytab-location: /etc/krb5/app.keytab
 * </pre>
 */
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        String defaultRole,
        Map<String, String> roleMappings,
        Kerberos kerberos,
        DevUser devUser) {

    public AppSecurityProperties {
        defaultRole = defaultRole == null ? "USER" : defaultRole;
        roleMappings = roleMappings == null ? Map.of() : roleMappings;
        kerberos = kerberos == null ? new Kerberos(null, null, false) : kerberos;
        devUser = devUser == null ? new DevUser(null, null) : devUser;
    }

    public record Kerberos(String servicePrincipal, String keytabLocation, boolean debug) {
    }

    /** Fake user for the "localdev" profile only. */
    public record DevUser(String name, List<String> groups) {
        public DevUser {
            name = name == null ? "devuser" : name;
            groups = groups == null ? List.of() : groups;
        }
    }
}
