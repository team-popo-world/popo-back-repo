package com.popoworld.backend.market.config;

import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.ProductLabel;
import com.popoworld.backend.market.entity.ProductStatus;
import com.popoworld.backend.market.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component //이 클래스를 Bean으로 자동 등록
@RequiredArgsConstructor
@Slf4j
public class NpcProductInitializer {
    private final ProductRepository productRepository;


    @PostConstruct //Bean이 생성된 후 자동으로 실행된다.
    @Transactional //이 메서드 전체를 하나의 트랜잭션으로 실행한다. 중간 실패시 롤백
    public void initializeNpcProducts(){
        if(productRepository.findByUserIsNull().isEmpty()){
            log.info("NPC 상품 초기화 시작 ");

            //Arrays.asList() -> 배열을 List로 바꿔준다
            List<Product> npcProducts = Arrays.asList(
                    new Product(null, null, "당근", 100, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/carrot_i3xbjj",
                            ProductStatus.REGISTERED, 10, ProductLabel.SNACK, null),
                    new Product(null, null, "물고기", 200, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/fish_hqsqfs",
                            ProductStatus.REGISTERED, 15, ProductLabel.SNACK, null),
                    new Product(null, null, "빵", 150, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/bread_nykeqe",
                            ProductStatus.REGISTERED, 12, ProductLabel.SNACK, null),
                    new Product(null, null, "사과", 80, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/apple_dyzpm6",
                            ProductStatus.REGISTERED, 8, ProductLabel.SNACK, null),
                    new Product(null, null, "수박", 300, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/watermelon_vrrndc",
                            ProductStatus.REGISTERED, 20, ProductLabel.SNACK, null),
                    new Product(null, null, "브로콜리", 120, -1,
                            "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/broccoli_nmpcqu",
                            ProductStatus.REGISTERED, 14, ProductLabel.SNACK, null)
            );
            productRepository.saveAll(npcProducts);
            log.info("NPC 상품 {}개 초기화 완료", npcProducts.size());
        }
    }

}
