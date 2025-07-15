package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.auth.models.Users;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByLogin(String login);

}
