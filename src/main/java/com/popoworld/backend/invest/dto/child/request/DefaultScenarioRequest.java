package com.popoworld.backend.invest.dto.child.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultScenarioRequest {
    private String chapterId;
    private String story;
    private String summary;
    private Boolean isCustom;
}
