package com.Back;

import com.Back.product.entity.Product;
import com.Back.product.repository.ProductRepository;
import com.Back.product.service.ProductChatService;
import com.Back.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class BackApplicationTests {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductChatService productChatService;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("JPA 테스트")
    void t1(){
        assertDoesNotThrow(()->{
            Product product = new Product();
            product.setName("테스트 상품");

            product.addKeyword("키워드1");
            product.addKeyword("키워드2");

            productRepository.save(product);
        });
    }

    @Test
    @DisplayName("Vector embedding 테스트")
    void t2(){
        assertDoesNotThrow(()->{
            Product product = new Product();
            product.setName("테스트 상품");
            product.setEmbedding(new float[384]);
            productRepository.save(product);

            Optional<Product> found = productRepository.findById(product.getId());
            assertTrue(found.isPresent());
            assertNotNull(found.get().getEmbedding());
        });
    }

    @Test
    @DisplayName("ProductService - Create 테스트")
    void t3() {
        String name = "노트북";
        List<String> keywords = List.of("전자기기", "컴퓨터", "휴대용");

        Product product = productService.create(name, keywords);

        assertNotNull(product.getId());
        assertEquals(name, product.getName());
        assertEquals(3, product.getKeywords().size());

        Optional<Product> found = productService.findById(product.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEmbedding());
    }

    @Test
    @DisplayName("ProductService - Read 테스트")
    void t4() {
        Product created = productService.create("스마트폰", List.of("전자기기", "통신"));

        Optional<Product> found = productService.findById(created.getId());
        List<Product> all = productService.findAll();

        assertTrue(found.isPresent());
        assertEquals("스마트폰", found.get().getName());
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("ProductService - Update 테스트")
    void t5() {
        Product created = productService.create("태블릿", List.of("전자기기"));

        Product updated = productService.update(created.getId(), "아이패드", List.of("애플", "태블릿", "전자기기"));

        assertEquals("아이패드", updated.getName());
        assertEquals(3, updated.getKeywords().size());

        Optional<Product> found = productService.findById(updated.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEmbedding());
    }

    @Test
    @DisplayName("ProductService - Delete 테스트")
    void t6() {
        Product created = productService.create("삭제용상품", List.of("테스트"));
        Long id = created.getId();

        productService.delete(id);

        Optional<Product> found = productService.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("ProductService - KNN Search basic test with keywords")
    void t7() {
        productService.create("Gaming Laptop", List.of("gaming", "high-performance", "graphics-card"));
        productService.create("Business Laptop", List.of("office", "document", "lightweight"));
        productService.create("Gaming Mouse", List.of("gaming", "high-sensitivity", "RGB"));

        List<Product> results = productService.knnSearch(List.of("gaming", "computer"), 3);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        System.out.println("KNN Search Results (keywords: gaming, computer):");
        results.forEach(r -> System.out.println(" - " + r.getName()));
    }

    @Test
    @DisplayName("ProductService - KNN Search with product keywords")
    void t8() {
        Product coffee = productService.create("Americano", List.of("coffee", "caffeine", "beverage"));
        Product tea = productService.create("Green Tea", List.of("tea", "catechin", "beverage"));
        Product juice = productService.create("Orange Juice", List.of("fruit", "vitamin", "beverage"));

        List<String> coffeeKeywords = List.of("coffee", "caffeine", "beverage");
        List<Product> results = productService.knnSearch(coffeeKeywords, 3);

        assertFalse(results.isEmpty());
        List<Long> resultIds = results.stream().map(Product::getId).toList();
        assertTrue(resultIds.contains(coffee.getId()), "Americano should be in results");

        System.out.println("Search with coffee keywords:");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ": " + results.get(i).getName());
        }
    }

    @Test
    @DisplayName("ProductService - findSimilarProducts test")
    void t9() {
        Product laptop = productService.create("MacBook Pro", List.of("laptop", "apple", "development"));
        Product phone = productService.create("iPhone 15", List.of("smartphone", "apple", "communication"));
        Product tablet = productService.create("iPad Pro", List.of("tablet", "apple", "drawing"));
        Product headphone = productService.create("AirPods Max", List.of("headphone", "apple", "music"));

        List<Product> results = productService.findSimilarProducts(laptop.getId(), 3);

        assertFalse(results.isEmpty());
        List<Long> resultIds = results.stream().map(Product::getId).toList();
        assertFalse(resultIds.contains(laptop.getId()), "MacBook should NOT be in results (self excluded)");

        System.out.println("Similar products to MacBook Pro:");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ": " + results.get(i).getName());
        }
    }

    @Test
    @DisplayName("ProductService - KNN Search clothing category")
    void t10() {
        Product jacket = productService.create("Winter Puffer Jacket", List.of("outerwear", "winter", "warm"));
        Product tshirt = productService.create("Cotton T-Shirt", List.of("top", "summer", "casual"));
        Product jeans = productService.create("Blue Jeans", List.of("bottom", "denim", "casual"));
        Product coat = productService.create("Wool Coat", List.of("outerwear", "winter", "formal"));

        List<Product> results = productService.knnSearch(List.of("outerwear", "winter", "warm"), 4);

        assertFalse(results.isEmpty());
        List<Long> resultIds = results.stream().map(Product::getId).toList();
        List<Long> winterIds = List.of(jacket.getId(), coat.getId());
        boolean hasWinterClothes = resultIds.stream().anyMatch(winterIds::contains);
        assertTrue(hasWinterClothes, "Winter clothes should be in results");

        System.out.println("Clothing search (winter keywords):");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ": " + results.get(i).getName());
        }
    }

    @Test
    @DisplayName("ProductService - KNN Search k limit verification")
    void t11() {
        productService.create("Product 1", List.of("test", "first"));
        productService.create("Product 2", List.of("test", "second"));
        productService.create("Product 3", List.of("test", "third"));
        productService.create("Product 4", List.of("test", "fourth"));
        productService.create("Product 5", List.of("test", "fifth"));

        List<Product> results = productService.knnSearch(List.of("test", "product"), 3);

        assertTrue(results.size() <= 3, "Should not return more than k results");

        System.out.println("k limit verification (k=3):");
        System.out.println("Number of results: " + results.size());
    }

    @Test
    @DisplayName("ProductService - KNN Search empty keywords handling")
    void t12() {
        List<Product> results = productService.knnSearch(List.of(), 3);

        assertNotNull(results);
        assertTrue(results.isEmpty(), "Empty keywords should return empty list");
        System.out.println("Empty keywords handling: result count = " + results.size());
    }

    @Test
    @DisplayName("ChatMemory - 단기 메모리 테스트 (동일 userId)")
    void t13() {
        productService.create("노트북", List.of("전자기기", "컴퓨터"));
        String userId = "test-user-1";

        var response1 = productChatService.chat("노트북 추천해줘", userId);
        assertNotNull(response1);
        assertNotNull(response1.message());
        System.out.println("첫 번째 응답: " + response1.message());

        var response2 = productChatService.chat("그 중에서 제일 추천할만한건?", userId);
        assertNotNull(response2);
        assertNotNull(response2.message());
        System.out.println("두 번째 응답: " + response2.message());
    }

    @Test
    @DisplayName("ChatMemory - 다른 userId는 별도 메모리 사용")
    void t14() {
        productService.create("스마트폰", List.of("전자기기", "모바일"));
        productService.create("노트북", List.of("전자기기", "컴퓨터"));
        String userId1 = "user-A";
        String userId2 = "user-B";

        var responseA1 = productChatService.chat("스마트폰 추천해줘", userId1);
        assertNotNull(responseA1);
        System.out.println("User A 첫 번째 응답: " + responseA1.message());

        var responseB1 = productChatService.chat("노트북 추천해줘", userId2);
        assertNotNull(responseB1);
        System.out.println("User B 첫 번째 응답: " + responseB1.message());

        var responseA2 = productChatService.chat("방금 추천해준 것 중에서 가장 좋은건?", userId1);
        assertNotNull(responseA2);
        System.out.println("User A 두 번째 응답: " + responseA2.message());
    }

    @Test
    @DisplayName("ChatMemory - 스트리밍 채팅 메모리 테스트")
    void t15() {
        productService.create("태블릿", List.of("전자기기", "터치스크린"));
        String userId = "stream-user";

        StringBuilder result = new StringBuilder();
        productChatService.chatStream("태블릿 추천해줘", userId)
                .doOnNext(result::append)
                .blockLast();

        assertFalse(result.toString().isEmpty());
        System.out.println("스트리밍 응답: " + result);
    }

}
