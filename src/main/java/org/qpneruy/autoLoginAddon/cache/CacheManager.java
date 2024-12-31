package org.qpneruy.autoLoginAddon.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class CacheManager<K, V> {
    private final Cache<K, V> cache;
    private final RemovalListener<K, V> removalListener;

    @FunctionalInterface
    public interface RemovalListener<K, V> {
        void onRemoval(K key, V value);
    }

    private CacheManager(Builder<K, V> builder) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .maximumSize(builder.maximumSize)
                .expireAfterWrite(builder.duration, builder.timeUnit);

        if (builder.removalListener != null) {
            caffeine.removalListener((key, value, cause) -> {
                if (key != null && value != null) {
                    builder.removalListener.onRemoval((K) key, (V) value);
                }
            });
        }

        this.cache = caffeine.build();
        this.removalListener = builder.removalListener;
    }

    public V getOrCompute(K key, Function<K, V> mappingFunction) {
        V value = cache.get(key, mappingFunction);
        return value;
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public Set<K> getKeys() {
        return cache.asMap().keySet();
    }

    public void invalidate(K key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public static class Builder<K, V> {
        private long maximumSize = 200;
        private long duration = 30;
        private TimeUnit timeUnit = TimeUnit.MINUTES;
        private RemovalListener<K, V> removalListener;

        public Builder<K, V> maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<K, V> expireAfter(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder<K, V> removalListener(RemovalListener<K, V> listener) {
            this.removalListener = listener;
            return this;
        }

        public CacheManager<K, V> build() {
            return new CacheManager<>(this);
        }
    }
}