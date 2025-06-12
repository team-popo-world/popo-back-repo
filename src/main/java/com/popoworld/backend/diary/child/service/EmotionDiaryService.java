package com.popoworld.backend.diary.child.service;

import com.popoworld.backend.diary.child.dto.DiaryListResponse;
import com.popoworld.backend.diary.child.dto.DiaryTodayRequest;
import com.popoworld.backend.diary.child.entity.EmotionDiary;
import com.popoworld.backend.diary.child.repository.EmotionDiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotionDiaryService {
    private final EmotionDiaryRepository emotionDiaryRepository;

    //감정일기 등록
    @Transactional
    public void createEmotionDiary(UUID childId, DiaryTodayRequest request){
        //입력값 검증
        validateRequest(request);

        //오늘 이미 일기가 있는지 확인
        LocalDate today = LocalDate.now();
//        if(emotionDiaryRepository.existsByChildIdAndCreatedAt(childId,today)){
//            throw new IllegalArgumentException("오늘 이미 감정일기를 작성했습니다.");
//        }

        EmotionDiary emotionDiary =new EmotionDiary(
                null,
                childId,
                request.getDescription(),
                request.getEmotion(),
                null // createdAt은 @prepersist에서 자동설정
        );
        emotionDiaryRepository.save(emotionDiary);
    }
    //감정일기 입력값 검증
    private void validateRequest(DiaryTodayRequest request){
        if(request.getEmotion()==null){
            throw new IllegalArgumentException("감정은 필수입니다.");
        }
        if(request.getDescription().length()>100){
            throw new IllegalArgumentException("일기 내용은 100자를 초과할 수 없습니다.");
        }
    }

    //특정 아이의 모든 감정 일기 조회
    public List<DiaryListResponse>getEmotionDiariesByChildId(UUID childId){
        List<EmotionDiary> diaries=emotionDiaryRepository.findByChildIdOrderByCreatedAtDesc(childId);
        return diaries.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }


    private DiaryListResponse convertToResponseDto(EmotionDiary emotionDiary){
        return DiaryListResponse.builder()
                .emotionDiaryId(emotionDiary.getEmotionDiaryId())
                        .childId(emotionDiary.getChildId())
                .emotion(emotionDiary.getEmotion())
                .description(emotionDiary.getDescription())
                .createdAt(emotionDiary.getCreatedAt())
        .build();
    }
}
