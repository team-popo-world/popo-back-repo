package com.popoworld.backend.webpush.repository;

import com.popoworld.backend.webpush.entity.WebPush;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PushRepository extends JpaRepository<WebPush, Long> {
    Optional<WebPush> findByUserId(UUID userId);
}
