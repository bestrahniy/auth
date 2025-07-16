package com.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.auth.dto.AuthorisateUserDto;
import com.auth.dto.RegisterNewUserDto;
import com.auth.dto.UserRolesDto;
import com.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(
    name = "user controller",
    description = "provides endpoints for register and authentification user in project"
)
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Register new user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (validation failed)"),
            @ApiResponse(responseCode = "409", description = "User already exists (duplicate email/username)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping("/signup")
    public ResponseEntity<?> registerNewUser(@RequestBody RegisterNewUserDto registerNewUserDto) throws Exception {
        return ResponseEntity.ok(userService.register(registerNewUserDto));
    }

    @Operation(
        summary = "Authenticate user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful (returns JWT token)"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (wrong username/password)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/signin")
    public ResponseEntity<?> authenticationNewUser(@RequestBody AuthorisateUserDto authorisateUserDto) throws Exception {
        return ResponseEntity.ok(userService.authorisateUser(authorisateUserDto));
    }

    @Operation(
        summary = "add user the new roles",
        responses = {
            @ApiResponse(responseCode = "200", description = "add role successful (returns JWT token)"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (wrong username/password)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/user-roles/save")
    public ResponseEntity<?> addRolesDeal(
        @RequestHeader("Authorization") String adminToken,
        @Valid @RequestBody UserRolesDto userRolesDto) throws Exception {
        return ResponseEntity.ok(userService.addNewRoleUser(userRolesDto));
    }

}
