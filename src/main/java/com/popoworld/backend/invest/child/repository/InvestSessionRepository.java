package com.popoworld.backend.invest.child.repository;

import com.popoworld.backend.invest.child.entity.InvestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvestSessionRepository extends JpaRepository<InvestSession, UUID> {
}