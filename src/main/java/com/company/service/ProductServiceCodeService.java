package com.company.service;

import static com.company.exception.model.constant.ErrorMessage.PRODUCT_SERVICE_CODE_ALREADY_EXISTS_MESSAGE;
import static com.company.exception.model.constant.ErrorMessage.PRODUCT_SERVICE_CODE_NOT_FOUND_MESSAGE;

import com.company.dao.cache.ProductServiceCodeCacheRepository;
import com.company.dao.entity.ProductServiceCodeEntity.Fields;
import com.company.dao.repository.ProductServiceCodeRepository;
import com.company.exception.AlreadyExistException;
import com.company.exception.DataNotFoundException;
import com.company.mapper.ProductServiceCodeMapper;
import com.company.model.dto.request.ProductServiceCodeRequest;
import com.company.model.dto.response.ProductServiceCodeResponse;
import com.company.model.enums.ClassificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class ProductServiceCodeService {

    private final ProductServiceCodeCacheRepository cacheRepository;
    private final ProductServiceCodeMapper productServiceCodeMapper;
    private final ProductServiceCodeRepository productServiceCodeRepository;

    public List<ProductServiceCodeResponse> findAll() {
        List<ProductServiceCodeResponse> cachedList = cacheRepository.findAll();
        if (!CollectionUtils.isEmpty(cachedList)) {
            return cachedList;
        }

        List<ProductServiceCodeResponse> dbList = productServiceCodeRepository.findAll().stream()
                .map(productServiceCodeMapper::toResponse)
                .toList();

        if (!CollectionUtils.isEmpty(dbList)) {
            cacheRepository.saveAll(dbList);
        }

        return dbList;
    }

    public List<ProductServiceCodeResponse> findAllByType(ClassificationType type) {
        List<ProductServiceCodeResponse> cachedList = cacheRepository.findByType(type);
        if (!CollectionUtils.isEmpty(cachedList)) {
            return cachedList;
        }

        List<ProductServiceCodeResponse> dbList = productServiceCodeRepository.findAllByType(type).stream()
                .map(productServiceCodeMapper::toResponse)
                .toList();

        if (!CollectionUtils.isEmpty(dbList)) {
            dbList.forEach(cacheRepository::saveOne);
        }

        return dbList;
    }

    public ProductServiceCodeResponse createCode(ProductServiceCodeRequest request) {
        boolean existsByCode = productServiceCodeRepository.existsByCode(request.getCode());
        if (existsByCode) {
            throw AlreadyExistException.of(PRODUCT_SERVICE_CODE_ALREADY_EXISTS_MESSAGE, Fields.code, request.getCode());
        }

        var savedEntity = productServiceCodeRepository.save(productServiceCodeMapper.toEntity(request));
        var response = productServiceCodeMapper.toResponse(savedEntity);
        cacheRepository.saveOne(response);

        return response;
    }

    public ProductServiceCodeResponse updateCode(Long id, ProductServiceCodeRequest request) {
        var entity = productServiceCodeRepository.findById(id)
                .orElseThrow(() -> DataNotFoundException.of(PRODUCT_SERVICE_CODE_NOT_FOUND_MESSAGE, Fields.id, id));

        productServiceCodeMapper.updateEntity(request, entity);

        var savedEntity = productServiceCodeRepository.save(entity);

        var response = productServiceCodeMapper.toResponse(savedEntity);
        cacheRepository.saveOne(response);

        return response;
    }
}