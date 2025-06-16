package com.popoworld.backend.invest.dto.parent.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ScenarioListDTO {
    private int page;
    private int size;
    private UUID childId;
}
