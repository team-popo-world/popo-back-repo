package com.popoworld.backend.invest.child.repository;

import com.popoworld.backend.invest.child.entity.InvestChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository

public interface InvestChapterRepository extends JpaRepository<InvestChapter, String> {
}

