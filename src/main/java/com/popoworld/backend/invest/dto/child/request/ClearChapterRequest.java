package com.popoworld.backend.invest.dto.child.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClearChapterRequest {
    private String chapterId;
    private String sessionId;
    private Boolean success;
    private Integer profit;
}
