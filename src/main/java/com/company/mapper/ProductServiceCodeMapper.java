package com.company.mapper;

import com.company.dao.entity.ProductServiceCodeEntity;
import com.company.model.dto.request.ProductServiceCodeRequest;
import com.company.model.dto.response.ProductServiceCodeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductServiceCodeMapper {

    ProductServiceCodeEntity toEntity(ProductServiceCodeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductServiceCodeRequest request, @MappingTarget ProductServiceCodeEntity entity);

    ProductServiceCodeResponse toResponse(ProductServiceCodeEntity productServiceCode);
}
