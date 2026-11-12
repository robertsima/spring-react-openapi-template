package com.example_project_name.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.example_project_name.generated.api.SessionApi;
import com.example_project_name.generated.model.CurrentUser;
import com.example_project_name.security.RoleMapper;

// GET /me - who the Windows SSO layer says the caller is, and their application roles.
// The frontend uses this instead of decoding a token.
@RestController
public class SessionController implements SessionApi {

    @Override
    public ResponseEntity<CurrentUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser user = new CurrentUser()
                .username(auth.getName())
                .roles(auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(a -> a.startsWith(RoleMapper.ROLE_PREFIX))
                        .distinct()
                        .toList());
        return ResponseEntity.ok(user);
    }
}
