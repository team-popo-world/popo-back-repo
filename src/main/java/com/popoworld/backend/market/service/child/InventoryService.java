package com.popoworld.backend.market.service.child;

import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    //인벤토리 조회
    public List<InventoryItemResponse>getUserInventory(UUID userId){
        List<Inventory>inventoryItems = inventoryRepository.findByUser_UserId(userId);
        return inventoryItems.stream()
                .map(InventoryItemResponse::fromEntity)
                .toList();
    }

}
