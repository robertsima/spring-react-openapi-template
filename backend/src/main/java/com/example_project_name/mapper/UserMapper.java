package com.example_project_name.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example_project_name.generated.model.UserDTO;
import com.example_project_name.model.Users;

// Maps persistence entities to the OpenAPI-generated DTOs.
// Implementation is generated at compile time (target/generated-sources/annotations) and registered as a Spring bean.
@Mapper
public interface UserMapper {

    UserDTO toDto(Users user);

    List<UserDTO> toDtos(List<Users> users);
}
