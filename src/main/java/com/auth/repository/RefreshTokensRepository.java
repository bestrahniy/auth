package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.auth.models.RefreshTokens;

public interface RefreshTokensRepository extends JpaRepository<RefreshTokens, Long> {

}
