package com.example_project_name.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example_project_name.generated.model.UserDTO;
import com.example_project_name.mapper.UserMapper;
import com.example_project_name.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    //basic method implementation for getting user by name, returns a list of UserDTOs
    @Override
    public List<UserDTO> getUsersByName(String name) {
        return userMapper.toDtos(userRepository.findByName(name));
    }
}
