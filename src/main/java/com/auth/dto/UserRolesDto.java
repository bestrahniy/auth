package com.auth.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dto for add the new role to user
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRolesDto {

    private String login;

    private Set<String> roles;

}
