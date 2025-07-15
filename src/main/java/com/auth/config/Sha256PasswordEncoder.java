package com.auth.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import com.auth.models.Users;

/**
 * custom hashing and check password
 */
@Component
public class Sha256PasswordEncoder {

    /**
     * date format for sole in password
     */
    private DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy:MM::dd:HH:mm")
            .withZone(ZoneId.systemDefault());

    /**
     * custom hashing passwod with sole from login and create date user
     */
    public String encode(CharSequence rawPassword, String login, String createAt) throws Exception {
        String passwordSolt = login + "_" + createAt + "_" + rawPassword;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = messageDigest.digest(passwordSolt.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);

        for (int i = 0; i < encodedhash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedhash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * check current password with correct password
     * creating hash current password and both hashes are compared
     * @param rawPassword current password
     * @param user current user
     * @return true if password is correct
     * @throws Exception
     */
    public boolean matches(CharSequence rawPassword, Users user) throws Exception {
        String createAt = dateTimeFormatter.format(user.getCreatedAt());
        String currentPasswordHash = encode(rawPassword, user.getLogin(), createAt);
        return currentPasswordHash.equals(user.getPassword());
    }

}
