package com.example_project_name.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example_project_name.generated.api.UsersApi;
import com.example_project_name.generated.model.UserDTO;
import com.example_project_name.service.UserService;

import lombok.RequiredArgsConstructor;

// Contract-first: the path, params, validation and DTOs all come from openapi/openapi.yaml via the
// generated UsersApi interface. Only override the operation here - don't redeclare @GetMapping etc.
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<List<UserDTO>> getUsersByName(String name) {
        return ResponseEntity.ok(userService.getUsersByName(name)); // just an example endpoint for testing
    }
}
