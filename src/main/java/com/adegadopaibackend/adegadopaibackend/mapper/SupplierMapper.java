package com.adegadopaibackend.adegadopaibackend.mapper;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.SupplierResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Supplier;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);

    List<SupplierResponse> toResponseList(List<Supplier> suppliers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(CreateSupplierRequest supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateSupplierRequest req, @MappingTarget Supplier supplier);
}