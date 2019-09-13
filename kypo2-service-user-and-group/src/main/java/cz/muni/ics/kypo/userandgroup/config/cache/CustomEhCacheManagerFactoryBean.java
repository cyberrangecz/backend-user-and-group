package cz.muni.ics.kypo.userandgroup.config.cache;

import net.sf.ehcache.Ehcache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

public class CustomEhCacheManagerFactoryBean extends EhCacheManagerFactoryBean {
    @Override
    public net.sf.ehcache.CacheManager getObject() {
        net.sf.ehcache.CacheManager cacheManager = super.getObject();
        cacheManager.addDecoratedCache(new ConcurrentMapCollectionHandlingDecoratedCache(new ConcurrentMapCache("users")));
        return cacheManager;
    }
}

