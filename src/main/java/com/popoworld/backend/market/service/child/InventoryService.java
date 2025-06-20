// InventoryService.java
package com.popoworld.backend.market.service.child;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import com.popoworld.backend.market.dto.child.UseItemRequest;
import com.popoworld.backend.market.dto.child.UseItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.repository.InventoryRepository;
import com.popoworld.backend.market.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // 자녀용: 본인 인벤토리 조회
    public List<InventoryItemResponse> getUserInventory(UUID userId) {
        List<Inventory> inventoryItems = inventoryRepository.findByUser_UserId(userId);
        return inventoryItems.stream()
                .filter(inventory -> inventory.getStock() > 0) // 수량이 0인 것 제외
                .map(InventoryItemResponse::fromEntity)
                .toList();
    }

    // 부모용: 자녀 인벤토리 조회 (부모 상품만)
    public List<InventoryItemResponse> getChildInventoryForParent(UUID childId, UUID parentId) {
        // 1. 자녀 확인 및 권한 검증
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀를 찾을 수 없습니다."));

        // 2. 자녀 소유권 확인
        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }

        // 3. 자녀 인벤토리 조회 (부모 상품만 필터링)
        List<Inventory> inventoryItems = inventoryRepository.findByUser_UserId(childId);
        return inventoryItems.stream()
                .filter(inventory -> inventory.getStock() > 0)
                .filter(inventory -> inventory.getProduct().getUser() != null) // ✅ NPC 상품 제외
                .map(InventoryItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UseItemResponse useItem(UseItemRequest request, UUID childId) {
        // 1. productId로 인벤토리에서 해당 아이템 조회
        Inventory inventory = inventoryRepository
                .findByUser_UserIdAndProduct_ProductId(childId, request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 상품입니다."));

        Product product = inventory.getProduct();

        // 2. NPC 상품은 사용 불가
        if (product.getUser() == null) {
            throw new IllegalArgumentException("NPC 상품은 인벤토리에서 사용할 수 없습니다. 포포 키우기에서 사용해주세요.");
        }

        // 3. 부모 상품만 사용 가능
        // 인벤토리에서 완전 삭제
        inventoryRepository.delete(inventory);

        // 상품 상태를 승인 대기로 변경
        product.setState(ProductStatus.USED);
        productRepository.save(product);

        log.info("✅ 부모 상품 사용 - 승인 대기 상태로 변경: {}", product.getProductName());

        return new UseItemResponse(
                "상품을 사용했습니다. 부모님의 승인을 기다리고 있습니다.",
                0
        );
    }
}