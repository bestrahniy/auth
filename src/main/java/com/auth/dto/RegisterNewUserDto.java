package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dto for registration new user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterNewUserDto {

    private String login;

    private String password;

    private String email;

}
