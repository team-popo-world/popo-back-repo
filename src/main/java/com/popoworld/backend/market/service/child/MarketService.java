package com.popoworld.backend.market.service.child;

import com.popoworld.backend.User.User;
import com.popoworld.backend.market.dto.child.MarketItemDTO;
import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.repository.InventoryRepository;
import com.popoworld.backend.market.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public List<MarketItemDTO> getItemsByType(String type) {
        List<Product> products;
        UUID childId = getCurrentUserId(); // 자녀 로그인 기준

        switch (type) {
            case "npc":
                products = productRepository.findByUserIsNull();
                break;
            case "parent":
                products = productRepository.findByUser(childId);
                break;
            case "inventory":
                products = inventoryRepository.findByChild(childId).stream()
                        .map(Inventory::getProduct)
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("잘못된 타입입니다.");
        }

        return products.stream()
                .map(MarketItemDTO::fromEntity)
                .toList();
    }

    public void purchaseItem(PurchaseRequestDTO request) {
        if (request.getAmount() == 0) {
            inventoryRepository.deleteById(request.getId());
        } else {
            Inventory inventory = inventoryRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 아이템 없음"));
            inventory.setStock(request.getAmount());
            inventoryRepository.save(inventory);
        }
    }

}
