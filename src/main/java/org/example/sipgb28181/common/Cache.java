package org.example.sipgb28181.common;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Cache {
    LRUCache<Object, Object> cacheObj = CacheUtil.newLRUCache(10000);
}
