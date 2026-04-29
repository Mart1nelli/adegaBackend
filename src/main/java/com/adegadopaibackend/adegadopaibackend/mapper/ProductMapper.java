package com.adegadopaibackend.adegadopaibackend.mapper;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ProductResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.entity.Review;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "stockHistory", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "stockHistory", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateProductRequest req, @MappingTarget Product product);

    @Mapping(target = "averageRating", expression = "java(calculateAverage(product.getReviews()))")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    default Double calculateAverage(List<Review> reviews) {
        if ( reviews == null || reviews.isEmpty() ) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
