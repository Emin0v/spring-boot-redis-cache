package com.company.dao.cache;

import static com.company.model.constant.RedisCacheKeyConstants.HASH_KEY;
import static com.company.model.constant.RedisCacheKeyConstants.INDEX_ALL_KEY;
import static com.company.model.constant.RedisCacheKeyConstants.INDEX_CATEGORY_PREFIX;
import static com.company.model.constant.RedisCacheKeyConstants.TTL_DAYS;

import com.company.model.dto.response.ProductServiceCodeResponse;
import com.company.model.enums.ClassificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
                .collect(Collectors.toMap(code -> code.getId().toString(), Function.identity()));

        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                operations.opsForHash().putAll(HASH_KEY, codesMap);
                operations.expire(HASH_KEY, TTL_DAYS, TimeUnit.DAYS);

                String[] allIds = codes.stream().map(c -> c.getId().toString()).toArray(String[]::new);
                operations.opsForSet().add(INDEX_ALL_KEY, allIds);
                operations.expire(INDEX_ALL_KEY, TTL_DAYS, TimeUnit.DAYS);

                Map<ClassificationType, List<String>> idsByCategory = codes.stream()
                        .collect(Collectors.groupingBy(
                                ProductServiceCodeResponse::getType,
                                Collectors.mapping(code -> code.getId().toString(), Collectors.toList())));

                idsByCategory.forEach((type, ids) -> {
                    String indexKey = INDEX_CATEGORY_PREFIX.concat(type.name());
                    operations.opsForSet().add(indexKey, ids.toArray(new String[0]));
                    operations.expire(indexKey, TTL_DAYS, TimeUnit.DAYS);
                });

                return operations.exec();
            }
        });
    }

    public void saveOne(ProductServiceCodeResponse code) {
        String idStr = code.getId().toString();
        String categoryIndexKey = INDEX_CATEGORY_PREFIX.concat(code.getType().name());

        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                operations.opsForHash().put(HASH_KEY, idStr, code);
                operations.opsForSet().add(INDEX_ALL_KEY, idStr);
                operations.opsForSet().add(categoryIndexKey, idStr);

                operations.expire(HASH_KEY, TTL_DAYS, TimeUnit.DAYS);
                operations.expire(INDEX_ALL_KEY, TTL_DAYS, TimeUnit.DAYS);
                operations.expire(categoryIndexKey, TTL_DAYS, TimeUnit.DAYS);

                return operations.exec();
            }
        });
    }

    public List<ProductServiceCodeResponse> findAll() {
        Set<Object> allIds = redisTemplate.opsForSet().members(INDEX_ALL_KEY);
        if (CollectionUtils.isEmpty(allIds)) {
            return Collections.emptyList();
        }
        return fetchFromHash(allIds);
    }

    public List<ProductServiceCodeResponse> findByType(ClassificationType type) {
        String indexKey = INDEX_CATEGORY_PREFIX.concat(type.name());
        Set<Object> ids = redisTemplate.opsForSet().members(indexKey);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
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