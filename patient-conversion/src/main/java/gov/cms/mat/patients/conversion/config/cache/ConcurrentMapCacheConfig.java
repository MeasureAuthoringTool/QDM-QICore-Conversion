package gov.cms.mat.patients.conversion.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@Slf4j
public class ConcurrentMapCacheConfig {

    @Value("${cache-names}")
    private List<String> names;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<ConcurrentMapCache> caches = createCaches();
        log.info("Created caches: {}", names);

        cacheManager.setCaches(caches);

        return cacheManager;
    }

    private List<ConcurrentMapCache> createCaches() {
        return names.stream()
                .map(ConcurrentMapCache::new)
                .collect(Collectors.toList());
    }
}
