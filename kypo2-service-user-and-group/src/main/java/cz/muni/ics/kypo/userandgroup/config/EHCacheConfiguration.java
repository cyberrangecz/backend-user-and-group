package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.config.cache.CustomEhCacheManagerFactoryBean;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class EHCacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users");

        //        //CacheManager manager = new EhCacheCacheManager(ehCacheCacheManager().getObject());
//        net.sf.ehcache.CacheManager manager  = ehCacheCacheManager().getObject();
//        Ehcache ehcache = manager.getEhcache("users");
//        manager.replaceCacheWithDecoratedCache(ehcache, );
//        new BlockingCache()
    }

//    @Bean
//    public EhCacheManagerFactoryBean ehCacheCacheManager() {
//        EhCacheManagerFactoryBean factory = new CustomEhCacheManagerFactoryBean();
//        factory.setConfigLocation(new ClassPathResource("ehcache.xml"));
//        factory.setShared(true);
//        return factory;
//    }
}







