package com.popoworld.backend.User.repository;

import com.popoworld.backend.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.parent.userId = :parentId AND u.role = 'Child'")
    List<User> findAllChildrenByParentId(@Param("parentId") UUID parentId);

    boolean existsByParentCode(String parentCode);

    Optional<User> findByParentCodeAndRole(String parentCode, String role);


    List<UUID> findAllChildrenByRole(String child);
}
