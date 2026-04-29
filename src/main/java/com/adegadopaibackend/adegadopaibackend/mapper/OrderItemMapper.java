package com.adegadopaibackend.adegadopaibackend.mapper;

import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateOrderItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderItemResponse;
import com.adegadopaibackend.adegadopaibackend.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    OrderItem toEntity(CreateOrderItemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntity(UpdateOrderItemRequest req, @MappingTarget OrderItem orderItem);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", expression = "java(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0)")
    OrderItemResponse toResponse(OrderItem item);
}