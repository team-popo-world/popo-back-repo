// MarketService.java
package com.popoworld.backend.market.service.child;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.child.PurchaseItemRequest;
import com.popoworld.backend.market.dto.child.PurchaseItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.repository.InventoryRepository;
import com.popoworld.backend.market.repository.ProductRepository;
import com.popoworld.backend.market.service.PurchaseHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
    private final PurchaseHistoryService purchaseHistoryService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public List<MarketItemResponse> getItemsByType(String type) {
        List<Product> products;
        UUID childId = getCurrentUserId();

        switch (type) {
            case "npc":
                // NPC 상품: 항상 구매 가능
                products = productRepository.findByUserIsNull();
                break;
            case "parent":
                // 부모 상품: REGISTERED 상태이고 재고가 있는 것만
                products = productRepository.findByTargetChildId(childId)
                        .stream()
                        .filter(p -> p.getState() == ProductStatus.REGISTERED)
                        .filter(p -> p.getProductStock() > 0)
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("잘못된 타입입니다.");
        }

        return products.stream()
                .map(MarketItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PurchaseItemResponse purchaseProduct(PurchaseItemRequest request, UUID childId) {
        // 1. 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 2. 사용자 조회
        User user = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. 구매 수량 결정
        int purchaseAmount;
        if (product.getUser() == null) {
            // NPC 상품: 요청한 수량만큼 구매 (기본값 1)
            purchaseAmount = (request.getAmount() != null) ? request.getAmount().intValue() : 1;
            if (purchaseAmount <= 0) {
                throw new IllegalArgumentException("구매 수량은 1개 이상이어야 합니다.");
            }
        } else {
            // 부모 상품: 무조건 1개만
            purchaseAmount = 1;
        }

        // 4. 총 가격 계산
        int totalPrice = product.getProductPrice() * purchaseAmount;

        // 5. 포인트 부족 체크
        if (user.getPoint() < totalPrice) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // 6. 부모 상품 재고 체크 (중복 구매 체크 불필요 - 재고 0이면 상점에서 안보임)
        if (product.getUser() != null && product.getProductStock() <= 0) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        // 7. 포인트 차감
        user.setPoint(user.getPoint() - totalPrice);
        userRepository.save(user);

        // 8. 부모 상품인 경우 재고 차감 및 상태 변경
        if (product.getUser() != null) {
            product.setProductStock(0); // 부모 상품은 1개 → 0개
            product.setState(ProductStatus.PURCHASED);
            productRepository.save(product);
        }

        // 9. 인벤토리에 추가
        addToInventory(user, product, purchaseAmount);

        // 10. 구매 이력 기록
        purchaseHistoryService.logPurchase(product, purchaseAmount, childId);

        return new PurchaseItemResponse(user.getPoint(), purchaseAmount, totalPrice);
    }

    private void addToInventory(User user, Product product, int amount) {
        log.info("=== 인벤토리 추가 시작 ===");
        log.info("사용자 ID: {}, 상품 ID: {}, 수량: {}", user.getUserId(), product.getProductId(), amount);

        try {
            if (product.getUser() == null) {
                // NPC 상품: 기존 인벤토리가 있으면 수량 추가, 없으면 새로 생성
                Optional<Inventory> existingInventory = inventoryRepository
                        .findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());

                if (existingInventory.isPresent()) {
                    Inventory inventory = existingInventory.get();
                    inventory.setStock(inventory.getStock() + amount);
                    inventoryRepository.save(inventory);
                    log.info("✅ 기존 NPC 인벤토리 수량 추가 완료");
                } else {
                    Inventory newInventory = Inventory.builder()
                            .user(user)
                            .product(product)
                            .stock(amount)
                            .build();
                    inventoryRepository.save(newInventory);
                    log.info("✅ 새 NPC 인벤토리 생성 완료");
                }
            } else {
                // 부모 상품: 항상 새로 생성 (중복 구매 불가능하므로)
                Inventory newInventory = Inventory.builder()
                        .user(user)
                        .product(product)
                        .stock(1) // 부모 상품은 항상 1개
                        .build();
                inventoryRepository.save(newInventory);
                log.info("✅ 부모 상품 인벤토리 생성 완료");
            }
        } catch (Exception e) {
            log.error("❌ 인벤토리 처리 실패: {}", e.getMessage());
            throw new RuntimeException("인벤토리 처리 중 오류가 발생했습니다.");
        }
    }
}