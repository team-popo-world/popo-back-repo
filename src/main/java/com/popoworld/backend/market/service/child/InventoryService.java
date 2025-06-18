package com.popoworld.backend.market.service.child;

import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import com.popoworld.backend.market.dto.child.UseItemRequest;
import com.popoworld.backend.market.dto.child.UseItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.ProductUsage;
import com.popoworld.backend.market.repository.InventoryRepository;
import com.popoworld.backend.market.repository.ProductUsageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductUsageRepository productUsageRepository;
    //인벤토리 조회
    public List<InventoryItemResponse>getUserInventory(UUID userId){
        List<Inventory>inventoryItems = inventoryRepository.findByUser_UserId(userId);
        return inventoryItems.stream()
                .filter(inventory -> inventory.getStock()>0)//재고 0인것 제외
                .map(InventoryItemResponse::fromEntity)
                .toList();
    }

    //상품 사용
    @Transactional
    public UseItemResponse useItem(UseItemRequest request,UUID childId){

        //1. 인벤토리에서 해당 상품 조회
       Inventory inventory = inventoryRepository.findByUser_UserIdAndProduct_ProductId(childId,request.getProductId())
               .orElseThrow(()->new IllegalArgumentException("보유하지 않은 상품입니다."));

       // 2. 부모 상품만 사용 가능 (NPC 상품은 포포키우기에서만 가능)
        if(inventory.getProduct().getUser()==null){
            throw new IllegalArgumentException("NPC상품은 사용할 수 없습니다.");
        }

        // 3. 수량 확인
        if(inventory.getStock()< request.getAmount()){
            throw new IllegalArgumentException("보유 수량 부족합니다.");
        }

        // 4. 인벤토리에서 수량 차감
        inventory.setStock(inventory.getStock()- request.getAmount());
        inventoryRepository.save(inventory);

        // 5. 사용 내역 기록
        ProductUsage usage = ProductUsage.builder()
                .child(inventory.getUser())
                 .product(inventory.getProduct())
                .usedAmount(request.getAmount())
                .build();

        productUsageRepository.save(usage);

        return new UseItemResponse(
            inventory.getStock(),
                request.getAmount()
                );


    }

}
