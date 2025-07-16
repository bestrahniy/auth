package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.auth.models.Users;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByLogin(String login);

    @Transactional
    void deleteByLogin(String login);

    @Query("select u from Users u left join fetch u.roles where u.login = :login")
    Optional<Users> findByLoginWithRoles(@Param("login") String login);

}
