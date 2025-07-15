package com.auth.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import com.auth.models.Users;
import com.auth.services.UserService;
import lombok.AllArgsConstructor;

/**
 * custom auntefication provider
 */
@Component
@AllArgsConstructor
public class CustomAunteficationProvider implements AuthenticationProvider {

    private final Sha256PasswordEncoder sha256PasswordEncoder;

    private final UserService userService;

    /**
     * authentificate user in project
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String password = authentication.getCredentials().toString();
        String login = authentication.getName();
        Users user = (Users) userService.loadUserByUsername(login);
        try {
            if (sha256PasswordEncoder.matches(password, user)) {
                return new UsernamePasswordAuthenticationToken(login, password, user.getAuthorities());
            }
        } catch (Exception e) {
            throw new AuthenticationServiceException("Aunthentification faled");
        }
        throw new BadCredentialsException("Aunthentification faled");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
