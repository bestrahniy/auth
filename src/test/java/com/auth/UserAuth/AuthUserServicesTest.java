package com.auth.UserAuth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.auth.dto.AuthorisateUserDto;
import com.auth.dto.RegisterNewUserDto;
import com.auth.dto.UserRolesDto;
import com.auth.models.RoleType;
import com.auth.models.Roles;
import com.auth.models.Users;
import com.auth.repository.RoleRepository;
import com.auth.repository.UserRepository;
import com.auth.services.JwtTokenService;
import com.auth.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.security.Key;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;

@SpringBootTest(
    properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:0/.well-known/jwks.json",
        "security.jwt.secret_key=b55a3f4a6400c4ef85c16187653713004986bace196af0d78c24b0d1ca26cd9a"
    }
)
@Testcontainers
@AutoConfigureMockMvc
@Import(AuthUserServicesTest.JwtDecoderConfig.class)
public class AuthUserServicesTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    String adminToken;

    private String secretkey = "b55a3f4a6400c4ef85c16187653713004986bace196af0d78c24b0d1ca26cd9a";

    private Key key;

    @Container
    private final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () ->  "classpath:db/db.changelog-master.yaml");
    }

    @BeforeEach
    void adminInit() throws InvalidKeyException, DecoderException {
        userRepository.deleteByLogin("admin");
        Users user = Users.builder()
            .login("admin")
            .passwordHash("admin")
            .email("admin@gmail.com")
            .build();
        Set<Roles> adminRoles = Set.of(roleRepository.findByRole(RoleType.ADMIN)
            .orElseThrow(() -> new EntityNotFoundException("role not found")));
        userRepository.save(user);

        adminToken = jwtTokenService.genrateAccessToken(user.getUsername(), adminRoles);

        byte[] keyBytes = org.apache.commons.codec.binary.Hex.decodeHex(secretkey.toCharArray());
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    @TestConfiguration
    static class JwtDecoderConfig {

        private String secretKey = "b55a3f4a6400c4ef85c16187653713004986bace196af0d78c24b0d1ca26cd9a";

        @Bean
        public JwtDecoder init() throws DecoderException {
            byte[] keyBytes = org.apache.commons.codec.binary.Hex
                            .decodeHex(secretKey.toCharArray());
            return NimbusJwtDecoder
                    .withSecretKey(new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256"))
                    .build();
        }
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

    @Test
    void whenPutNewRole_ShouldUpdateinDb() throws Exception {
        RegisterNewUserDto registerNewUserDto = new RegisterNewUserDto("vany", "12345", "vany@gmail.com");
        mockMvc.perform(put("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerNewUserDto)))
            .andExpect(status().is2xxSuccessful());
        
        UserRolesDto userRolesDto = new UserRolesDto();
        userRolesDto.setLogin("vany");
        userRolesDto.setRoles(Set.of("SUPERUSER", "USER"));

        mockMvc.perform(post("/auth/user-roles/save")
                .header("Authorization","Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRolesDto)))
            .andExpect(status().is2xxSuccessful());

        Users user = userRepository.findByLoginWithRoles("vany")
            .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        Set<RoleType> roles = user.getRoles().stream()
            .map(Roles::getRole)
            .collect(Collectors.toSet());
        assertEquals(Set.of(RoleType.SUPERUSER, RoleType.USER), roles);
    }

}
