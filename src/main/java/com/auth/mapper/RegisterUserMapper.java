package com.auth.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.auth.dto.RegisterNewUserDto;
import com.auth.models.Users;

/**
 * mapper for creating new entity of user
 */
@Component
public class RegisterUserMapper {

    public Users registerNewUserMapper(RegisterNewUserDto registerNewUserDto) {
        return Users.builder()
            .login(registerNewUserDto.getLogin())
            .passwordHash(registerNewUserDto.getPassword())
            .email(registerNewUserDto.getEmail())
            .createdAt(Instant.now())
            .build();
    }

}
