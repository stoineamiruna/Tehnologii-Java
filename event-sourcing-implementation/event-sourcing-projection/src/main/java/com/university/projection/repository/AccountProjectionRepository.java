package com.university.projection.repository;

import com.university.projection.model.AccountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountProjectionRepository extends JpaRepository<AccountProjection, String> {
}