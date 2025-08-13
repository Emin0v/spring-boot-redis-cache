package com.company.dao.repository;

import com.company.dao.entity.ProductServiceCodeEntity;
import com.company.model.enums.ClassificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductServiceCodeRepository extends JpaRepository<ProductServiceCodeEntity, Long> {

    List<ProductServiceCodeEntity> findAllByType(ClassificationType type);

    boolean existsByCode(String code);
}
