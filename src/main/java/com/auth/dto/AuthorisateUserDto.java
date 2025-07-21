package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dto for aithorisate user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorisateUserDto {

    private String login;
    private String password;

}
