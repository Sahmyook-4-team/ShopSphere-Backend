package com.shopsphere.shopsphere_web.controller;

import com.shopsphere.shopsphere_web.dto.ProductDTO;
import com.shopsphere.shopsphere_web.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.List;
import java.util.Collections; // 추가

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
  
    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품 ID
     * @return 조회된 상품 정보 (ProductDTO.Response)와 200 OK 상태 코드,
     *         상품이 없는 경우 404 Not Found
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO.Response> getProductById(@PathVariable Integer productId) {
        ProductDTO.Response product = productService.getProduct(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 모든 상품 목록을 조회합니다.
     *
     * @return 조회된 모든 상품 목록과 200 OK 상태 코드,
     *         상품이 없는 경우 빈 리스트와 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO.Response>> getAllProducts() {
        List<ProductDTO.Response> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 새로운 상품을 생성합니다.
     *
     * @param session 현재 HTTP 세션
     * @param request 생성할 상품 정보 (ProductDTO.CreateRequest)
     * @return 생성된 상품 정보 (ProductDTO.Response)와 201 Created 상태 코드
     */
    @PostMapping()
    public ResponseEntity<?> createProduct(
            HttpSession session,
            @RequestBody ProductDTO.CreateRequest request) {
        try {
            // 세션에서 사용자 ID 가져오기
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
            }

            // 서비스 레이어에 사용자 ID와 함께 상품 생성 요청
            ProductDTO.Response response = productService.createProduct(userId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 스택 트레이스 출력
            String errorMessage = "상품 등록 중 오류가 발생했습니다: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (원인: " + e.getCause().getMessage() + ")";
            }
            return ResponseEntity.status(500).body(Map.of(
                "message", errorMessage,
                "error", e.getClass().getName(),
                "details", e.getMessage()
            ));
        }
    }

    /**
     * 특정 카테고리에 속한 모든 상품 목록을 조회합니다.
     *
     * @param categoryId 조회할 카테고리 ID
     * @return 해당 카테고리의 상품 목록 (List<ProductDTO.Response>)과 200 OK 상태 코드
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO.Response>> getProductsByCategory(@PathVariable Integer categoryId) {
        List<ProductDTO.Response> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * 현재 로그인한 판매자가 등록한 모든 상품 목록을 조회합니다.
     *
     * @param session 현재 HTTP 세션
     * @return 판매자의 상품 목록 (List<ProductDTO.Response>)과 200 OK 상태 코드
     */
    @GetMapping("/seller/me")
    public ResponseEntity<List<ProductDTO.Response>> getMyProducts(HttpSession session) {
        List<ProductDTO.Response> products = productService.getProductsBySeller(session.getAttribute("userId").toString());
        return ResponseEntity.ok(products);
    }

   
    /**
     * 기존 상품 정보를 수정합니다.
     *
     * @param productId 수정할 상품 ID
     * @param request   수정할 상품 정보 (ProductDTO.UpdateRequest)
     * @param session   현재 HTTP 세션
     * @return 수정된 상품 정보 (ProductDTO.Response)와 200 OK 상태 코드,
     *         상품이 없는 경우 404 Not Found,
     *         권한이 없는 경우 403 Forbidden
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer productId,
            @RequestBody ProductDTO.UpdateRequest request,
            HttpSession session) {
        // 세션에서 사용자 ID 가져오기
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 상품 소유자 확인 및 수정
        try {
            ProductDTO.Response response = productService.updateProduct(userId, productId, request);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", "상품을 수정할 권한이 없습니다."));
        }
    }

    /**
     * 특정 상품을 삭제합니다.
     *
     * @param productId 삭제할 상품 ID
     * @param session   현재 HTTP 세션
     * @return 204 No Content 상태 코드 (성공 시),
     *         401 Unauthorized (로그인 필요),
     *         403 Forbidden (권한 없음)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Integer productId,
            HttpSession session) {
        // 세션에서 사용자 ID 가져오기
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 상품 소유자 확인 및 삭제
        try {
            productService.deleteProduct(userId, productId);
            return ResponseEntity.ok().body(Map.of("message", "상품이 성공적으로 삭제되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", "상품을 삭제할 권한이 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

     /**
     * 상품명 또는 카테고리명으로 상품을 검색합니다.
     *
     * @param keyword 검색어 (프론트엔드에서 query로 보냈다면 @RequestParam("query") String keyword)
     * @return 검색된 상품 목록 (List<ProductDTO.Response>)
     */
    @GetMapping("/search")
public ResponseEntity<List<ProductDTO.Response>> searchProducts(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "sort", defaultValue = "musinsa_recommend") String sortOption) { // sort 파라미터 추가
    if (keyword == null || keyword.trim().isEmpty()) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    List<ProductDTO.Response> products = productService.searchProductsByKeyword(keyword.trim(), sortOption); // 서비스에 sortOption 전달
    return ResponseEntity.ok(products);
}
}
