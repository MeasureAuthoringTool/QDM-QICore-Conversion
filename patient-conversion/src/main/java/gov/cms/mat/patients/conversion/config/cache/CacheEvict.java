package gov.cms.mat.patients.conversion.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Slf4j
public class CacheEvict {
    private final CacheManager cacheManager;

    public CacheEvict(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // On the hour every hour
    @Scheduled(cron = "0 0 * * * *")
    public void evictAll() {
        cacheManager.getCacheNames()
                .forEach(this::evict);
    }

    private void evict(String name) {
        Cache cache = cacheManager.getCache(name);

        if (cache == null) {
            log.error("Cache is null: {}", name); // Should never happen
        } else {
            cache.clear();
            log.info("Cleared cache: {}", name);
        }
    }
}
