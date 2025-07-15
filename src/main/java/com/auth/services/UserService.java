package com.auth.services;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.auth.config.Sha256PasswordEncoder;
import com.auth.dto.AuthResponseDto;
import com.auth.dto.AuthorisateUserDto;
import com.auth.dto.RegisterNewUserDto;
import com.auth.mapper.RegisterUserMapper;
import com.auth.models.RoleType;
import com.auth.models.Users;
import com.auth.repository.RoleRepository;
import com.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

/**
 * manage user
 */
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final RegisterUserMapper registerUserMapper;

    private final JwtTokenService jwtTokenService;

    private final Sha256PasswordEncoder sha256PasswordEncoder;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AuthenticationConfiguration authenticationConfiguration;

    private final DealConnect dealConnect;

    /**
     * register new user in priject and hashing his password by custom hash with soled
     * @param registerNewUserDto dto with data of new user
     * @return save new user
     * @throws Exception
     */
    @Transactional
    public Users register(RegisterNewUserDto registerNewUserDto) throws Exception {
        DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy:MM::dd:HH:mm")
                .withZone(ZoneId.systemDefault());
        Users user = getNewregisterUser(registerNewUserDto);
        String createdAt = dateTimeFormatter.format(user.getCreatedAt());

        String passwordHash = sha256PasswordEncoder.encode(
            user.getPassword(), user.getLogin(), createdAt
        );

        user.setPasswordHash(passwordHash);
        user.setRoles(Set.of(
            roleRepository.findByRole(RoleType.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"))
        ));

        return userRepository.save(user);
    }

    /**
     * authentification user in system, creating refresh and access tokens
     * @param authorisateUserDto dto with login and passworg user
     * @return dto with access and refresh tokens
     * @throws Exception
     */
    public AuthResponseDto authorisateUser(AuthorisateUserDto authorisateUserDto) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authorisateUserDto.getLogin(), authorisateUserDto.getPassword())
        );
        Users user = userRepository.findByLogin(authorisateUserDto.getLogin())
            .orElseThrow(() -> new EntityNotFoundException("user not found"));

        String accessToken = jwtTokenService.genrateAccessToken(user.getLogin(), user.getRoles());
        String refreshToken = jwtTokenService.generateRefreshTocken(user.getLogin(), user.getRoles());

        jwtTokenService.saveRefreshToken(refreshToken);

        dealConnect.connectDeal(refreshToken);
    
        return new AuthResponseDto(accessToken, refreshToken);
    }

    public Users getNewregisterUser(RegisterNewUserDto registerNewUserDto) {
        return registerUserMapper.registerNewUserMapper(registerNewUserDto);
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        return userRepository.findByLogin(login)
            .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

}
