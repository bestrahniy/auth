package com.auth.services;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import javax.crypto.SecretKey;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.auth.models.RefreshTokens;
import com.auth.models.Roles;
import com.auth.models.Users;
import com.auth.repository.RefreshTokensRepository;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;

/**
 * manage jwt token
 */
@Service
public class JwtTokenService {

    @Value("${security.jwt.secret_key}")
    private String secretkey;

    @Value("${security.jwt.access_token_expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh_token_expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokensRepository refreshTokensRepository;

    private final UserService userService;

    public JwtTokenService(
        RefreshTokensRepository refreshTokensRepository,
        @Lazy UserService userService
    ) {
        this.refreshTokensRepository = refreshTokensRepository;
        this.userService = userService;
    }

    /**
     * getting bytes from secret key
     * @throws DecoderException
     */
    private SecretKey getSigningKey() throws DecoderException {
        byte[] keyBytes = org.apache.commons.codec.binary.Hex.decodeHex(secretkey.toCharArray());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * creating new access token with list of name role
     * @param userName login user
     * @param roles set
     * @return access token
     * @throws DecoderException
     * @throws InvalidKeyException
     */
    public String genrateAccessToken(String userName, Set<Roles> roles) throws InvalidKeyException, DecoderException {
        Instant now = Instant.now();
        List<String> rolesName = roles.stream()
            .map(role -> role.getRole().name())
            .toList();

        return Jwts.builder()
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
                .signWith(getSigningKey())
                .subject(userName)
                .claim("roles", rolesName)
                .compact();
    }

    /**
     * creating new refresh token with list of name role
     * @param userName login user
     * @param roles set
     * @return refresh token
     * @throws DecoderException
     * @throws InvalidKeyException
     */
    public String generateRefreshTocken(String userName, Set<Roles> roles) throws InvalidKeyException, DecoderException {
        Instant now = Instant.now();
        List<String> rolesName = roles.stream()
            .map(role -> role.getRole().name())
            .toList();

        return Jwts.builder()
                    .issuedAt(Date.from(now))
                    .notBefore(Date.from(now))
                    .expiration(Date.from(now.plusMillis(refreshTokenExpiration)))
                    .signWith(getSigningKey())
                    .subject(userName)
                    .claim("roles", rolesName)
                    .compact();
    }

    /**
     * parsing token and save their entity in db
     * @param refreshToken line of token
     * @return save token in db
     * @throws Exception
     */
    @Transactional
    public RefreshTokens saveRefreshToken(String refreshToken) throws Exception {
        Jws<Claims> claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(refreshToken);

        Claims body = claims.getPayload();
        String userName = body.getSubject();
        Date createAt  = body.getIssuedAt();
        Date expires  = body.getExpiration();
        java.sql.Date sqlExpires = new java.sql.Date(expires.getTime());
        Users user = (Users) userService.loadUserByUsername(userName);

        RefreshTokens token = RefreshTokens.builder()
                .tokenHash(refreshToken)
                .createAt(Instant.ofEpochMilli(createAt.getTime()))
                .expireAt(sqlExpires)
                .revorked(false)
                .user(user)
                .build();

        return refreshTokensRepository.save(token);
    }

}
