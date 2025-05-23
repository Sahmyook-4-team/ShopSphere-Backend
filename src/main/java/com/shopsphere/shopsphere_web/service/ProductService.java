package com.shopsphere.shopsphere_web.service;

import com.shopsphere.shopsphere_web.dto.ProductCategoryDTO;
import com.shopsphere.shopsphere_web.dto.ProductDTO;
import com.shopsphere.shopsphere_web.dto.ProductOptionDTO;
import com.shopsphere.shopsphere_web.dto.ProductImageDTO;
import com.shopsphere.shopsphere_web.entity.*;
import com.shopsphere.shopsphere_web.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductOptionRepository optionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductDTO.Response createProduct(String userId, ProductDTO.CreateRequest request) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = Product.builder()
                .category(categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found")))
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .user(seller)
                .salesVolume(0)
                .createdAt(LocalDateTime.now())
                .build();

        product = productRepository.save(product);

        // Save options
        if (request.getOptions() != null) {
            for (ProductOptionDTO.CreateRequest optionRequest : request.getOptions()) {
                ProductOption option = ProductOption.builder()
                        .product(product)
                        .size(optionRequest.getSize())
                        .stockQuantity(optionRequest.getStockQuantity())
                        .additionalPrice(optionRequest.getAdditionalPrice())
                        .build();
                optionRepository.save(option);
            }
        }

        return convertToResponse(product);
    }

    public ProductDTO.Response getProduct(Integer productId) {
        return productRepository.findById(productId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    public List<ProductDTO.Response> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategory_Id(categoryId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductDTO.Response> getProductsBySeller(String userId) {
        return productRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO.Response updateProduct(Integer productId, ProductDTO.UpdateRequest request) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setName(request.getName());
                    product.setDescription(request.getDescription());
                    product.setPrice(request.getPrice());
                    product.setStockQuantity(request.getStockQuantity());
                    product.setImageUrl(request.getImageUrl());

                    // Update options
                    if (request.getOptions() != null) {
                        for (ProductOptionDTO.UpdateRequest optionRequest : request.getOptions()) {
                            if (optionRequest.getId() != null) {
                                optionRepository.findById(optionRequest.getId())
                                        .ifPresent(option -> {
                                            option.setSize(optionRequest.getSize());
                                            option.setStockQuantity(optionRequest.getStockQuantity());
                                            option.setAdditionalPrice(optionRequest.getAdditionalPrice());
                                        });
                            }
                        }
                    }

                    return convertToResponse(product);
                })
                .orElse(null);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }

    private ProductDTO.Response convertToResponse(Product product) {
        ProductDTO.Response response = new ProductDTO.Response();
        response.setId(product.getId());
        response.setCategory(convertToCategoryResponse(product.getCategory()));
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setSalesVolume(product.getSalesVolume());

        // Convert options
        List<ProductOptionDTO.Response> optionResponses = optionRepository.findByProduct_Id(product.getId())
                .stream()
                .map(this::convertToOptionResponse)
                .collect(Collectors.toList());
        response.setOptions(optionResponses);

        // Convert images
        List<ProductImageDTO.Response> imageResponses = imageRepository.findByProduct_Id(product.getId())
                .stream()
                .map(this::convertToImageResponse)
                .collect(Collectors.toList());
        response.setImages(imageResponses);

        return response;
    }

    private ProductOptionDTO.Response convertToOptionResponse(ProductOption option) {
        ProductOptionDTO.Response response = new ProductOptionDTO.Response();
        response.setId(option.getId());
        response.setSize(option.getSize());
        response.setStockQuantity(option.getStockQuantity());
        response.setAdditionalPrice(option.getAdditionalPrice());
        return response;
    }

    private ProductImageDTO.Response convertToImageResponse(ProductImage image) {
        ProductImageDTO.Response response = new ProductImageDTO.Response();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setCreatedAt(image.getCreatedAt());
        return response;
    }

    private ProductCategoryDTO.Response convertToCategoryResponse(ProductCategory category) {
        ProductCategoryDTO.Response response = new ProductCategoryDTO.Response();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreatedAt());
        if (category.getParent() != null) {
            response.setParent(convertToCategoryResponse(category.getParent()));
        }
        return response;
    }
}
