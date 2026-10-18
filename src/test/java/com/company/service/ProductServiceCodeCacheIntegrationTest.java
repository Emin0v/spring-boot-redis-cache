package com.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.config.RedisConfig;
import com.company.dao.cache.ProductServiceCodeCacheRepository;
import com.company.dao.entity.ProductServiceCodeEntity;
import com.company.dao.repository.ProductServiceCodeRepository;
import com.company.mapper.ProductServiceCodeMapper;
import com.company.model.constant.RedisCacheKeyConstants;
import com.company.model.dto.request.ProductServiceCodeRequest;
import com.company.model.enums.ClassificationType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ProductServiceCodeCacheIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private final Map<Long, ProductServiceCodeEntity> rows = new LinkedHashMap<>();
    private LettuceConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redis;
    private ProductServiceCodeCacheRepository cache;
    private ProductServiceCodeService service;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        RedisConfig configuration = new RedisConfig();
        redis = configuration.redisTemplate(connectionFactory, configuration.objectMapper());
        try (var connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
        cache = new ProductServiceCodeCacheRepository(configuration.objectMapper(), redis);
        var database = mock(ProductServiceCodeRepository.class);
        when(database.findAll()).thenAnswer(invocation -> new ArrayList<>(rows.values()));
        when(database.findAllByType(any())).thenAnswer(invocation -> rows.values().stream()
                .filter(row -> row.getType() == invocation.getArgument(0)).toList());
        when(database.findById(anyLong())).thenAnswer(invocation -> Optional.ofNullable(
                rows.get(invocation.getArgument(0))));
        when(database.save(any())).thenAnswer(invocation -> {
            ProductServiceCodeEntity row = invocation.getArgument(0);
            if (row.getId() == null) {
                row.setId(100L);
            }
            rows.put(row.getId(), row);
            return row;
        });
        service = new ProductServiceCodeService(cache,
                Mappers.getMapper(ProductServiceCodeMapper.class), database);
        rows.put(1L, entity(1L, ClassificationType.PRODUCT));
        rows.put(2L, entity(2L, ClassificationType.SERVICE));
    }

    @AfterEach
    void closeConnection() {
        connectionFactory.destroy();
    }

    @Test
    void categoryReadDoesNotMakeAllCodesCacheLookComplete() {
        assertThat(service.findAllByType(ClassificationType.PRODUCT)).hasSize(1);
        assertThat(service.findAll()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void createWithColdCacheDoesNotHideExistingDatabaseRows() {
        service.createCode(new ProductServiceCodeRequest("new", "New", ClassificationType.PRODUCT));
        assertThat(service.findAll()).extracting("id").containsExactlyInAnyOrder(1L, 2L, 100L);
    }

    @Test
    void classificationChangeRemovesOldCategoryMembership() {
        service.findAll();
        service.updateCode(1L, new ProductServiceCodeRequest("code-1", "Moved", ClassificationType.SERVICE));
        assertThat(service.findAllByType(ClassificationType.PRODUCT)).isEmpty();
        assertThat(service.findAllByType(ClassificationType.SERVICE))
                .extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void missingHashEntryFallsBackInsteadOfReturningPartialResult() {
        service.findAll();
        redis.opsForHash().delete(RedisCacheKeyConstants.HASH_KEY, "2");
        assertThat(service.findAll()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void replacingFullSnapshotRemovesRowsFromOldCategoryIndexes() {
        service.findAll();
        rows.remove(2L);
        var mapper = Mappers.getMapper(ProductServiceCodeMapper.class);
        cache.saveAll(rows.values().stream().map(mapper::toResponse).toList());
        assertThat(service.findAllByType(ClassificationType.SERVICE)).isEmpty();
    }

    private ProductServiceCodeEntity entity(Long id, ClassificationType type) {
        ProductServiceCodeEntity entity = new ProductServiceCodeEntity();
        entity.setId(id);
        entity.setCode("code-" + id);
        entity.setName("Code " + id);
        entity.setType(type);
        return entity;
    }
}
