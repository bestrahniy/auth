package com.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.auth.models.Roles;
import com.auth.models.RoleType;

public interface RoleRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByRole(RoleType role);

}
