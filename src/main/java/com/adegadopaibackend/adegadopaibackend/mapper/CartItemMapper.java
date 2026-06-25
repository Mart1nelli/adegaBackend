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

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
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

    CartItemResponse toResponse(CartItem item);
}
