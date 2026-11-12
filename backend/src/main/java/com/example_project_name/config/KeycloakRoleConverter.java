package com.example_project_name.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

// Maps Keycloak realm roles (realm_access.roles) to Spring authorities so hasRole("ADMIN") works.
// Keycloak role "admin" -> authority "ROLE_ADMIN". Scope-based authorities (SCOPE_xxx) are kept as well.
// The frontend applies the same normalisation (frontend/src/utils/auth/auth.ts).
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return Stream.concat(scopes.convert(jwt).stream(), realmRoles(jwt).stream()).toList();
    }

    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> realmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> map) || !(map.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return ((Collection<Object>) roles).stream()
                .map(String::valueOf)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }
}
