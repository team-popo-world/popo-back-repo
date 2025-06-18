package com.popoworld.backend.market.service.parent;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketParentService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    public MarketItemResponse createParentProduct(CreateProductRequest request, UUID parentId){
        User child = userRepository.findById(request.getChildId()).orElseThrow(() -> new IllegalArgumentException("해당 child를 찾을 수 없습니다."));

        //상품 생성
        Product product = new Product(
                null,
                child,
                request.getProductName(),
                request.getProductPrice(),
                request.getProductStock(),
                request.getProductImage(),
                ProductStatus.REGISTERED,
                0
        );
        Product savedProduct = productRepository.save(product);
        return MarketItemResponse.fromEntity(savedProduct);
    }
}
