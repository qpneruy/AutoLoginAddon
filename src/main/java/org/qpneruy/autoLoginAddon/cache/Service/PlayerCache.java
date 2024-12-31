package org.qpneruy.autoLoginAddon.cache.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PlayerCache {
    private static final int CACHE_SIZE = 1000;
    private static final int CACHE_DURATION_HOURS = 24;
    @Getter
    private static final PlayerCache INSTANCE = new PlayerCache();
    private final Set<String> quickLookupCache;
    private final Cache<String, PlayerData> mainCache;

    private PlayerCache() {
        this.quickLookupCache = ConcurrentHashMap.newKeySet();
        this.mainCache = Caffeine.newBuilder()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(CACHE_DURATION_HOURS, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    public boolean isPlayerKnown(String uuid) {
        return quickLookupCache.contains(uuid);
    }

    public boolean isIpRegistered(String uuid, String ip) {
        String cacheKey = uuid + ":" + ip;
        return quickLookupCache.contains(cacheKey);
    }

    public void cachePlayer(String uuid) {
        quickLookupCache.add(uuid);
        mainCache.get(uuid, k -> new PlayerData(k, new HashSet<>(), LocalDateTime.now()));
    }

    public void cachePlayerIp(String uuid, String ip) {
        String cacheKey = uuid + ":" + ip;
        quickLookupCache.add(cacheKey);

        PlayerData playerData = mainCache.get(uuid, k -> new PlayerData(k, new HashSet<>(), LocalDateTime.now()));
        Set<String> updatedIps = new HashSet<>(playerData.ipAddresses());
        updatedIps.add(ip);
        mainCache.put(uuid, new PlayerData(uuid, updatedIps, playerData.lastLogin()));
    }

    public Set<String> getPlayerIps(String uuid) {
        PlayerData playerData = mainCache.getIfPresent(uuid);
        return playerData != null ? Collections.unmodifiableSet(playerData.ipAddresses()) : Collections.emptySet();
    }

    public void updatePlayerData(String uuid, PlayerData newData) {
        mainCache.put(uuid, newData);
    }

    public void removeIp(String uuid, String ip) {
        String cacheKey = uuid + ":" + ip;
        quickLookupCache.remove(cacheKey);

        PlayerData playerData = mainCache.getIfPresent(uuid);
        if (playerData != null) {
            Set<String> updatedIps = new HashSet<>(playerData.ipAddresses());
            updatedIps.remove(ip);
            mainCache.put(uuid, new PlayerData(uuid, updatedIps, playerData.lastLogin()));
        }
    }

    public void clearPlayerIps(String uuid) {
        PlayerData playerData = mainCache.getIfPresent(uuid);
        if (playerData != null) {
            playerData.ipAddresses().forEach(ip -> quickLookupCache.remove(uuid + ":" + ip));
            mainCache.put(uuid, new PlayerData(uuid, new HashSet<>(), playerData.lastLogin()));
        }
    }

    public record PlayerData(String uuid, Set<String> ipAddresses, LocalDateTime lastLogin) {
    }
}