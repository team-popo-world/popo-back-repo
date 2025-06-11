package com.popoworld.backend.User.repository;

import com.popoworld.backend.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByParentCode(String parentCode);

    List<User> findAllByParent(User parent);

    boolean existsByParentCode(String parentCode);
}
