package com.shopsphere.shopsphere_web.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
// ProductOptionDTO, ProductImageDTO, ProductCategoryDTO, UserDTO 등 필요한 DTO import

@Data
public class ProductDTO {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String description;
    private Integer price;
    private Integer stockQuantity;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String userId;
    private Integer salesVolume;
    private List<ProductOptionDTO> options;
    private List<ProductImageDTO> images;
    private String thumbnailUrl;


    @Data
    public static class CreateRequest {
        private Integer categoryId;
        private String name;
        private String description;
        private Integer price;
        private Integer stockQuantity;
        private String imageUrl; // 대표 이미지 URL
        private List<String> additionalImageUrls; // 추가 이미지 URL 목록 (ProductService에서 처리 방식 변경 필요)
        private List<ProductOptionDTO.CreateRequest> options;
    }

    @Data
    public static class UpdateRequest {
        private Integer categoryId;
        private String name;
        private String description;
        private Integer price;
        private Integer stockQuantity;
        private String imageUrl; // 대표 이미지 URL
        // 이미지 수정/삭제/추가를 위한 필드 추가 필요 (예: List<Integer> deletedImageIds, List<String> newImageUrls)
        private List<ProductOptionDTO.UpdateRequest> options;
    }

    @Data
    public static class Response {
        private Integer id;
        private ProductCategoryDTO.Response category; // ProductCategoryDTO.Response 타입으로 변경
        private String name;
        private String description;
        private Integer price;
        private Integer stockQuantity;
        private String imageUrl; // ProductImage로 통합 관리 시 이 필드는 제거 또는 대표 이미지 URL만 저장

        private LocalDateTime createdAt;
        private UserDTO.Response seller; // UserDTO.Response 타입으로 변경
        private Integer salesVolume;
        private List<ProductOptionDTO.Response> options; // ProductOptionDTO.Response 타입으로 변경
        private List<ProductImageDTO> images; // 모든 상품 이미지를 담는 리스트
        private Double averageRating;
        private Long reviewCount;
        private Long interestCount; // 예시 필드
    }
}