package com.example_project_name.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Turns Windows identities (account names and AD group names) into application roles using
 * app.security.role-mappings. Every authenticated user also gets ROLE_{default-role}.
 * Matching is case-insensitive because AD names are.
 */
@Component
public class RoleMapper {

    public static final String ROLE_PREFIX = "ROLE_";

    private final String defaultRole;
    private final Map<String, String> mappings;

    public RoleMapper(AppSecurityProperties properties) {
        this.defaultRole = properties.defaultRole().toUpperCase(Locale.ROOT);
        this.mappings = properties.roleMappings().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        e -> e.getKey().toLowerCase(Locale.ROOT),
                        e -> e.getValue().toUpperCase(Locale.ROOT)));
    }

    public Set<GrantedAuthority> authoritiesFor(Collection<String> identities) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + defaultRole));
        identities.stream()
                .map(id -> mappings.get(id.toLowerCase(Locale.ROOT)))
                .filter(role -> role != null)
                .forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
        return authorities;
    }

    /** Role for a single identity, or null if it isn't mapped. */
    public String roleFor(String identity) {
        return mappings.get(identity.toLowerCase(Locale.ROOT));
    }
}
