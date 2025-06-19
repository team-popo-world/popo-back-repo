package com.popoworld.backend.popoPet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class FeedingRequest {
    private List<FeedItem> feedItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedItem {  // 여기서는 static 가능
        private UUID productId;
        private Integer amount;
    }
}
