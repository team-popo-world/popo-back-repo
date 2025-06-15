package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestChapter;
import com.popoworld.backend.invest.entity.InvestScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvestChapterRepository extends JpaRepository<InvestChapter, String> {
    InvestChapter findByChapterId(String chapterId);
}

