package com.company.dao.cache;

import static com.company.model.constant.RedisCacheKeyConstants.HASH_KEY;
import static com.company.model.constant.RedisCacheKeyConstants.INDEX_ALL_KEY;
import static com.company.model.constant.RedisCacheKeyConstants.INDEX_CATEGORY_PREFIX;
import static com.company.model.constant.RedisCacheKeyConstants.TTL_DAYS;

import com.company.model.dto.response.ProductServiceCodeResponse;
import com.company.model.enums.ClassificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductServiceCodeCacheRepository {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAll(List<ProductServiceCodeResponse> codes) {
        Map<String, ProductServiceCodeResponse> codesMap = codes.stream()
                .collect(Collectors.toMap(code -> code.getId().toString(), code -> code));

        redisTemplate.opsForHash().putAll(HASH_KEY, codesMap);
        redisTemplate.expire(HASH_KEY, TTL_DAYS, TimeUnit.DAYS);

        String[] allIds = codes.stream().map(c -> c.getId().toString()).toArray(String[]::new);
        redisTemplate.opsForSet().add(INDEX_ALL_KEY, allIds);
        redisTemplate.expire(INDEX_ALL_KEY, TTL_DAYS, TimeUnit.DAYS);

        Map<ClassificationType, List<String>> idsByCategory = codes.stream()
                .collect(Collectors.groupingBy(
                        ProductServiceCodeResponse::getType,
                        Collectors.mapping(code -> code.getId().toString(), Collectors.toList())
                ));

        idsByCategory.forEach((type, ids) -> {
            String indexKey = INDEX_CATEGORY_PREFIX.concat(type.name());
            redisTemplate.opsForSet().add(indexKey, ids.toArray(new String[0]));
            redisTemplate.expire(indexKey, TTL_DAYS, TimeUnit.DAYS);
        });
    }

    public void saveOne(ProductServiceCodeResponse code) {
        String idStr = code.getId().toString();
        redisTemplate.opsForHash().put(HASH_KEY, idStr, code);

        redisTemplate.opsForSet().add(INDEX_ALL_KEY, idStr);
        redisTemplate.opsForSet().add(INDEX_CATEGORY_PREFIX.concat(code.getType().name()), idStr);
    }

    public List<ProductServiceCodeResponse> findAll() {
        Set<Object> allIds = redisTemplate.opsForSet().members(INDEX_ALL_KEY);
        if (CollectionUtils.isEmpty(allIds)) {
            return null;
        }
        return fetchFromHash(allIds);
    }

    public List<ProductServiceCodeResponse> findByType(ClassificationType type) {
        String indexKey = INDEX_CATEGORY_PREFIX.concat(type.name());
        Set<Object> ids = redisTemplate.opsForSet().members(indexKey);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        return fetchFromHash(ids);
    }

    private List<ProductServiceCodeResponse> fetchFromHash(Collection<Object> ids) {
        List<Object> hashValues = redisTemplate.opsForHash().multiGet(HASH_KEY, ids);

        return hashValues.stream()
                .filter(Objects::nonNull)
                .map(v -> objectMapper.convertValue(v, ProductServiceCodeResponse.class))
                .toList();
    }
}