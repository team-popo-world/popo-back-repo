// MarketParentService.java
package com.popoworld.backend.market.service.parent;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.parent.ApprovalItemResponse;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketParentService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public MarketItemResponse createParentProduct(CreateProductRequest request, UUID parentId) {
        User child = userRepository.findById(request.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다."));

        // 자녀 소유권 확인
        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }

        // 상품 생성 (부모 상품은 항상 재고 1개)
        Product product = new Product(
                null,
                child,
                request.getProductName(),
                request.getProductPrice(),
                1, // 부모 상품은 항상 1개
                request.getProductImage(),
                ProductStatus.REGISTERED,
                0, // 부모 상품은 경험치 없음
                request.getLabel(),
                null
        );

        Product savedProduct = productRepository.save(product);
        return MarketItemResponse.fromEntity(savedProduct);
    }

    public List<MarketItemResponse> getMyProducts(UUID parentId, UUID childId) {
        List<Product> products;

        if (childId != null) {
            products = productRepository.findByParentIdAndChildId(parentId, childId);
        } else {
            products = productRepository.findByParentId(parentId);
        }

        return products.stream()
                .filter(p -> p.getState() == ProductStatus.REGISTERED)
                .map(MarketItemResponse::fromEntity)
                .toList();
    }

    // 승인 대기 목록 조회 (상태: USED)
    public List<ApprovalItemResponse> getPendingApprovals(UUID parentId, UUID childId) {
        List<Product> pendingProducts;

        if (childId != null) {
            // 특정 자녀의 승인 대기 목록
            pendingProducts = productRepository.findPendingApprovalsByChildId(childId);
            // 해당 자녀가 정말 이 부모의 자녀인지 확인
            if (!pendingProducts.isEmpty()) {
                User child = pendingProducts.get(0).getUser();
                if (!child.getParent().getUserId().equals(parentId)) {
                    throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
                }
            }
        } else {
            // 모든 자녀의 승인 대기 목록
            pendingProducts = productRepository.findPendingApprovalsByParentId(parentId);
        }

        return pendingProducts.stream()
                .map(ApprovalItemResponse::fromEntity)
                .toList();
    }

    // 승인 완료 내역 조회 (상태: APPROVED)
    public List<ApprovalItemResponse> getApprovedHistory(UUID parentId, UUID childId) {
        List<Product> approvedProducts = productRepository
                .findApprovedUsageByParentId(parentId, childId);

        return approvedProducts.stream()
                .map(ApprovalItemResponse::fromEntity)
                .toList();
    }

    // 사용 승인 처리 (USED → APPROVED)
    @Transactional
    public void approveUsage(UUID productId, UUID childId, UUID parentId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 2. 자녀 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀를 찾을 수 없습니다."));

        // 3. 자녀 소유권 확인
        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }

        // 4. 상품-자녀 매칭 확인 (이 상품이 정말 해당 자녀의 것인지)
        if (!product.getUser().getUserId().equals(childId)) {
            throw new IllegalArgumentException("해당 자녀의 상품이 아닙니다.");
        }

        // 5. 상태 확인: USED 상태여야 승인 가능
        if (product.getState() != ProductStatus.USED) {
            throw new IllegalArgumentException("승인 대기 상태가 아닙니다.");
        }

        // 6. 승인 처리
        product.setState(ProductStatus.APPROVED);
        productRepository.save(product);

        log.info("✅ 상품 사용 승인 완료: 상품={}, 자녀={}, 부모={}",
                product.getProductName(),
                child.getName(),
                parentId);
    }

    @Transactional
    public void deleteParentProduct(UUID productId, UUID childId, UUID parentId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // NPC 상품 삭제 방지
        if (product.getUser() == null) {
            throw new IllegalArgumentException("NPC 상품은 삭제할 수 없습니다.");
        }

        // 자녀 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀를 찾을 수 없습니다."));

        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }

        // 상품-자녀 매칭 확인
        if (!product.getUser().getUserId().equals(childId)) {
            throw new IllegalArgumentException("해당 자녀용으로 등록된 상품이 아닙니다.");
        }

        // 구매되지 않은 상품만 삭제 가능
        if (product.getState() != ProductStatus.REGISTERED) {
            throw new IllegalArgumentException("이미 구매된 상품은 삭제할 수 없습니다.");
        }

        // 상태를 DISCONTINUED로 변경
        product.setState(ProductStatus.DISCONTINUED);
        productRepository.save(product);

        log.info("✅ 상품 삭제 완료: 상품={}, 자녀={}, 부모={}",
                product.getProductName(), childId, parentId);
    }
}