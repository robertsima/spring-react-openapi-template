package unit.com.example_project_name.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.example_project_name.security.AppSecurityProperties;
import com.example_project_name.security.RoleMapper;

class RoleMapperTest {

    private final RoleMapper roleMapper = new RoleMapper(new AppSecurityProperties(
            "user", Map.of("MYDOMAIN\\App-Admins", "admin"), null, null));

    @Test
    void everyUserGetsTheDefaultRole() {
        assertThat(roles(List.of("MYDOMAIN\\jdoe"))).containsExactly("ROLE_USER");
    }

    @Test
    void mappedGroupsAddRoles_caseInsensitive() {
        assertThat(roles(List.of("mydomain\\app-admins"))).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    private List<String> roles(List<String> identities) {
        return roleMapper.authoritiesFor(identities).stream().map(GrantedAuthority::getAuthority).toList();
    }
}
