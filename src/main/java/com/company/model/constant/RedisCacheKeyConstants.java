package com.company.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisCacheKeyConstants {

    public static final String HASH_KEY = "dict:codes";
    public static final String INDEX_ALL_KEY = "dict:index:all";
    public static final String INDEX_CATEGORY_PREFIX = "dict:index:category:";
    public static final long TTL_DAYS = 30;
}
