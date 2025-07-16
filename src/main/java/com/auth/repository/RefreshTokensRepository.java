package com.auth.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.auth.models.RefreshTokens;
import com.auth.models.Users;

public interface RefreshTokensRepository extends JpaRepository<RefreshTokens, Long> {

    List<RefreshTokens> findAllByUser(Users user);

}
