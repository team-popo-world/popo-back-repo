package com.popoworld.backend.popoPet.repository;

import com.popoworld.backend.popoPet.entity.PopoPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PopoPetRepository extends JpaRepository<PopoPet, UUID> {
    Optional<PopoPet> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

}
