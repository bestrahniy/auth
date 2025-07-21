package com.auth.dto;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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

    @NonNull
    @Size(min = 5, max = 20)
    private String login;

    @NonNull
    @Size(min = 7)
    private String password;

    @Email
    @NonNull
    private String email;

}
