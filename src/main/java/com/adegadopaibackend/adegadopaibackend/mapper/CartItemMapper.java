package com.adegadopaibackend.adegadopaibackend.mapper;

import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartItemResponse;
import com.adegadopaibackend.adegadopaibackend.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    CartItem toEntity(CreateCartItemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntity(UpdateCartItemRequest req, @MappingTarget CartItem cartItem);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productPrice", expression = "java(item.getProduct() != null ? item.getProduct().getPrice().doubleValue() : 0.0)")
    @Mapping(target = "total", expression = "java(item.getProduct() != null ? item.getProduct().getPrice().doubleValue() * item.getQuantity() : 0.0)")
    CartItemResponse toResponse(CartItem item);
}
