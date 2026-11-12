package integration.com.example_project_name.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import integration.com.example_project_name.DatabaseBaseIT;

@AutoConfigureMockMvc
class SessionControllerIT extends DatabaseBaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "MYDOMAIN\\jdoe", roles = {"USER", "ADMIN"})
    void meReturnsUserAndRoles() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("MYDOMAIN\\jdoe"))
                .andExpect(jsonPath("$.roles[?(@ == 'ROLE_ADMIN')]").exists());
    }

    @Test
    void unauthenticatedCallsAreRejected() throws Exception {
        // no SSO profile in tests -> fail-closed chain
        mockMvc.perform(get("/users").param("name", "Alice Johnson"))
                .andExpect(status().isUnauthorized());
    }
}
