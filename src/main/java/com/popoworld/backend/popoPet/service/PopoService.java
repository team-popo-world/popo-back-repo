package com.popoworld.backend.popoPet.service;

import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.repository.InventoryRepository;
import com.popoworld.backend.market.repository.ProductRepository;
import com.popoworld.backend.popoPet.dto.FeedingRequest;
import com.popoworld.backend.popoPet.dto.FeedingResponse;
import com.popoworld.backend.popoPet.dto.PopoFeedResponse;
import com.popoworld.backend.popoPet.entity.PopoPet;
import com.popoworld.backend.popoPet.repository.PopoPetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopoService {

    private final PopoPetRepository popoPetRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    //포포 먹이 조회
    public PopoFeedResponse getAvailableFeeds(UUID userId){

        //1. 포포 정보 조회
        PopoPet popo = getOrCreatePopo(userId);

        //2. NPC 타입 인벤토리 아이템만 조회
        List<Inventory> npcInventories = inventoryRepository.findByUser_UserId(userId)
                .stream()
                .filter(inventory -> inventory.getProduct().getUser()==null) //user가 null인건 npc상품
                .filter(inventory -> inventory.getStock()>0)
                .collect(Collectors.toList());

        //3. DTO 변환
        List<InventoryItemResponse> availableFeeds = npcInventories.stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());

        return PopoFeedResponse.builder()
                .currentLevel(popo.getLevel())
                .currentExperience(popo.getExperience())
                .totalExperience(popo.getTotalExperience())
                .availableFeeds(availableFeeds)
                .build();
    }
    //포포에게 먹이주기
    @Transactional
    public FeedingResponse feedPopo(UUID userId, FeedingRequest request){

        //1. 포포 정보 조회
        PopoPet popo = getOrCreatePopo(userId);
        int originalLevel = popo.getLevel();

        // 2. 먹이 처리
        List<String> fedItemNames = new ArrayList<>();
        int totalGainedExp = 0;


        for (FeedingRequest.FeedItem feedItem : request.getFeedItems()) {
            // 상품 조회
            Product product = productRepository.findById(feedItem.getProductId()).get();

            // 인벤토리에서 차감
            Inventory inventory = inventoryRepository.findByUser_UserIdAndProduct_ProductId(userId, feedItem.getProductId()).get();
            inventory.setStock(inventory.getStock() - 1);
            inventoryRepository.save(inventory);

            // 경험치 추가
            popo.addExperience(product.getExp());
            totalGainedExp += product.getExp();
            fedItemNames.add(product.getProductName());
        }

        // 3. 포포 저장
        popoPetRepository.save(popo);

        // 4. 응답 생성
        boolean levelUp = popo.getLevel() > originalLevel;
        String message = levelUp ?
                String.format("🎉 포포가 레벨 %d로 성장했어요!", popo.getLevel()) :
                "🍎 포포가 맛있게 먹었어요!";

        return FeedingResponse.builder()
                .newLevel(popo.getLevel())
                .currentExperience(popo.getExperience())
                .totalExperience(popo.getTotalExperience())
                .gainedExperience(totalGainedExp)
                .levelUp(levelUp)
                .fedItems(fedItemNames)
                .build();
    }

    //사용자가 포포가 이미 있다면 조회, 없으면 생성
    private PopoPet getOrCreatePopo(UUID userId){
        return popoPetRepository.findByUserId(userId)
                .orElseGet(()->{
                    PopoPet newPopo = PopoPet.createNewPopo(userId);
                    return popoPetRepository.save(newPopo);
                });
    }
}
