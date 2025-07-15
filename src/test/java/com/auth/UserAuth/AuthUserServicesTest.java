package com.auth.UserAuth;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.auth.dto.AuthorisateUserDto;
import com.auth.dto.RegisterNewUserDto;
import com.auth.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class AuthUserServicesTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () ->  "classpath:db/db.changelog-master.yaml");
    }

    @Test
    void UserRegistrer_ShouldExistsPasswordInDb() throws Exception {
        var registerNewUserDto = new com.auth.dto.RegisterNewUserDto("ilya", "12345", "bob@gmail.com");
        userService.register(registerNewUserDto);
        String password = jdbcTemplate.queryForObject(
            "SELECT password FROM users WHERE login = ?",
            new Object[]{ "ilya" },
            String.class
        );
        assertThat(password).isNotNull();
        assertThat(password).doesNotContain("12345");
    }

    @Test
    void UserAuthentification_ShouldBySuccessfully() throws Exception {
        RegisterNewUserDto registerNewUserDto = new RegisterNewUserDto("il", "12345", "bobik@gmail.com");
        mockMvc.perform(put("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerNewUserDto)))
            .andExpect(status().is2xxSuccessful());

        AuthorisateUserDto authorisateUserDto = new AuthorisateUserDto("il", "12345");
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorisateUserDto)))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.accessToken").exists());
    }

}
