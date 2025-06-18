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
                .map(MarketItemResponse::fromEntity)
                .toList();
    }
    // 🔥 사용 내역 조회
    public List<UsageHistoryResponse> getUsageHistory(UUID parentId, UUID childId) {
        List<ProductUsage> usageList = productUsageRepository.findByParentIdAndChildId(parentId, childId);
        return usageList.stream()
                .map(UsageHistoryResponse::fromEntity)
                .toList();
    }
    }

