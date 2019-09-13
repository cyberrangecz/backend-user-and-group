package cz.muni.ics.kypo.userandgroup.config.cache;

import org.springframework.cache.Cache;

import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;

public class ConcurrentMapCollectionHandlingDecoratedCache extends CollectionHandlingDecoratedCache {

    protected ConcurrentMapCollectionHandlingDecoratedCache(final Cache cache) {
        super(cache);
    }

    @Override
    protected boolean areAllKeysPresentInCache(Iterable<?> keys) {

        ConcurrentMap nativeCache = (ConcurrentMap) getNativeCache();

        return StreamSupport.stream(keys.spliterator(), false).allMatch(nativeCache::containsKey);
    }
}
