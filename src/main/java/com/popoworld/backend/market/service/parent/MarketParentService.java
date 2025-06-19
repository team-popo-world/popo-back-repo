package com.popoworld.backend.market.service.parent;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.dto.parent.UsageHistoryResponse;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.entity.ProductUsage;
import com.popoworld.backend.market.repository.ProductRepository;
import com.popoworld.backend.market.repository.ProductUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketParentService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductUsageRepository productUsageRepository;

    public MarketItemResponse createParentProduct(CreateProductRequest request, UUID parentId){
        User child = userRepository.findById(request.getChildId()).orElseThrow(() -> new IllegalArgumentException("해당 child를 찾을 수 없습니다."));
        // 🔥 추가 검증: 해당 자녀가 정말 이 부모의 자녀인지 확인
        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }
        //상품 생성
        Product product = new Product(
                null,
                child,
                request.getProductName(),
                request.getProductPrice(),
                request.getProductStock(),
                request.getProductImage(),
                ProductStatus.REGISTERED,
                0,
                request.getLabel()
        );
        Product savedProduct = productRepository.save(product);
        return MarketItemResponse.fromEntity(savedProduct);
    }

    // 🔥 수정된 메서드: childId 파라미터 추가
    public List<MarketItemResponse> getMyProducts(UUID parentId, UUID childId) {
        List<Product> products;

        if (childId != null) {
            // 특정 자녀용 상품만 조회
            products = productRepository.findByParentIdAndChildId(parentId, childId);
        } else {
            // 모든 자녀용 상품 조회
            products = productRepository.findByParentId(parentId);
        }

        return products.stream()
                .filter(p -> p.getState() == ProductStatus.REGISTERED)  // 🔥 먼저 필터링
                .map(MarketItemResponse::fromEntity)                     // 🔥 그 다음 변환
                .toList();
    }
    // 🔥 사용 내역 조회
    public List<UsageHistoryResponse> getUsageHistory(UUID parentId, UUID childId) {
        List<ProductUsage> usageList = productUsageRepository.findByParentIdAndChildId(parentId, childId);
        return usageList.stream()
                .map(UsageHistoryResponse::fromEntity)
                .toList();
    }

    // MarketParentService.java - deleteParentProduct 메서드 완전 교체

    @Transactional
    public void deleteParentProduct(UUID productId, UUID childId, UUID parentId) {

        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 2. NPC 상품 삭제 방지
        if (product.getUser() == null) {
            throw new IllegalArgumentException("NPC 상품은 삭제할 수 없습니다.");
        }

        // 3. 자녀 확인: 요청된 자녀가 실제로 이 부모의 자녀인지 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀를 찾을 수 없습니다."));

        if (!child.getParent().getUserId().equals(parentId)) {
            throw new IllegalArgumentException("본인의 자녀가 아닙니다.");
        }

        // 4. 상품-자녀 매칭 확인: 이 상품이 정말 해당 자녀용으로 등록된 상품인지 확인
        if (!product.getUser().getUserId().equals(childId)) {
            throw new IllegalArgumentException("해당 자녀용으로 등록된 상품이 아닙니다.");
        }

        // 🔥 5. 상태를 DISCONTINUED로 변경
        product.setState(ProductStatus.DISCONTINUED);
        productRepository.save(product);

    }
    }

