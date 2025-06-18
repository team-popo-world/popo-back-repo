package com.popoworld.backend.market.service.child;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.child.PurchaseItemRequest;
import com.popoworld.backend.market.dto.child.PurchaseItemResponse;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
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
                products = productRepository.findByUserIsNull();
                break;
            case "parent":
                // 🔥 메서드명 변경
                products = productRepository.findByTargetChildId(childId);
                break;
            default:
                throw new IllegalArgumentException("잘못된 타입입니다.");
        }

        return products.stream()
                .map(MarketItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PurchaseItemResponse purchaseProduct(PurchaseItemRequest request,UUID childId) {

        //1. 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(()-> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        //2. 사용자 조회
        User user = userRepository.findById(childId).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        //3. 총 가격 계산
        int totalPrice = request.getAmount() * product.getProductPrice();

        //4. 포인트 부족 체크
        if(user.getPoint()<totalPrice){
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        //5. 재고 부족 체크 (부모 상품이고, 무한재고가 아니고, 재고가 부족한 경우)
        if(product.getUser()!=null && product.getProductStock()!=-1 && product.getProductStock()<request.getAmount()){
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        //6. 포인트 차감
        user.setPoint(user.getPoint()-totalPrice);
        userRepository.save(user);

        //7. 재고 차감(부모 상품만)
        if(product.getUser()!=null&& product.getProductStock() != -1){
            product.setProductStock(product.getProductStock()-request.getAmount());
            productRepository.save(product);
        }

        //8. 인벤토리에 추가
        addToInventory(user,product,request.getAmount());

        purchaseHistoryService.logPurchase(product, request.getAmount(), childId);
        return new PurchaseItemResponse(user.getPoint(),request.getAmount(),totalPrice);

    }

    // 인벤토리에 아이템 추가 (JPA 버전)
    private void addToInventory(User user, Product product, int amount) {
        log.info("=== 인벤토리 추가 시작 ===");
        log.info("사용자 ID: {}, 상품 ID: {}, 수량: {}", user.getUserId(), product.getProductId(), amount);

        try {
            // 기존 인벤토리 찾기
            Optional<Inventory> existingInventory = inventoryRepository
                    .findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());

            if (existingInventory.isPresent()) {
                // 있으면 수량 추가
                Inventory inventory = existingInventory.get();
                inventory.setStock(inventory.getStock() + amount);
                inventoryRepository.save(inventory);
                log.info("✅ 기존 인벤토리 업데이트 완료");
            } else {
                // 없으면 새로 생성
                Inventory newInventory = Inventory.builder()
                        .user(user)
                        .product(product)
                        .stock(amount)
                        .build();
                inventoryRepository.save(newInventory);
                log.info("✅ 새 인벤토리 생성 완료");
            }
        } catch (Exception e) {
            log.error("❌ 인벤토리 처리 실패: {}", e.getMessage());
            throw new RuntimeException("인벤토리 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
}
