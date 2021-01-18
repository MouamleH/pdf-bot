package me.mouamle.bot.pdf.service;

/*
 * Copyright 2017 Hussain Al-Derry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Cache implementation with a periodic memory clean up process.
 *
 * @author Hussain Al-Derry
 * @version 1.0
 */
@Slf4j
public class ConcurrentCache<K, V> implements SnapshotProvider<K, V> {

    private final String name;
    private final Map<K, Holder<V>> mMap;
    private final long timeToLive;
    private final long cleanUpInterval;
    private final ScheduledExecutorService mExecutorService;

    private final AtomicLong lastCleanup = new AtomicLong(0);

    /**
     * @param elementTimeToLiveMillis The time (in milliseconds) each element stays alive after it was last accessed.
     * @param cleanUpIntervalMillis   The interval (in milliseconds) between cache clean ups.
     * @param cacheSize               The size of the cache.
     */
    public ConcurrentCache(String name, long elementTimeToLiveMillis, long cleanUpIntervalMillis, int cacheSize) {
        this.name = name;
        mMap = new ConcurrentHashMap<>(cacheSize);
        this.timeToLive = elementTimeToLiveMillis;
        this.cleanUpInterval = cleanUpIntervalMillis;
        this.mExecutorService = Executors.newSingleThreadScheduledExecutor();
        setupCleanUpProcess();
    }

    public long getLastCleanup() {
        return lastCleanup.get();
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public long getCleanUpInterval() {
        return cleanUpInterval;
    }

    public int size() {
        return mMap.size();
    }

    /**
     * Puts the specified value in the cache, overwrites any value previously mapped to the specified key.
     *
     * @param key   The key which the specified value is associated with.
     * @param value The value to be cached.
     */
    public void put(K key, V value) {
        mMap.put(key, new Holder<>(value));
    }

    /**
     * Puts the specified value in the cache, if a value is already mapped to the specified key that value is returned.
     *
     * @param key   The key which the specified value is associated with.
     * @param value The value to be cached.
     * @return The old value corresponding to the provided key
     */
    public V putIfAbsent(K key, V value) {
        Holder<V> vHolder = mMap.putIfAbsent(key, new Holder<>(value));
        return vHolder != null ? vHolder.getValue() : null;
    }

    /**
     * Returns the value associated with the specified key, if no mapping is found the method returns null.
     *
     * @param key The key associated with the value to be returned.
     * @return The value corresponding to the key if it exists, else null
     */
    public V get(K key) {
        Holder<V> mHolder = mMap.get(key);
        if (mHolder != null) {
            return mHolder.getValue();
        } else {
            return null;
        }
    }

    public V peek(K key) {
        Holder<V> mHolder = mMap.get(key);
        if (mHolder != null) {
            return mHolder.peek();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the Cache has a value associated with the specified key, else returns false;
     *
     * @param key The key to be checked
     * @return true if the cache has a value mapped to the given key.
     */
    public boolean containsKey(K key) {
        return mMap.containsKey(key);
    }

    /**
     * Removes the value associated with the specified key.
     *
     * @param key The key associated with the value to be removed.
     * @return If the value exists it's returned and removed, else null
     */
    public V remove(K key) {
        Holder<V> mHolder = mMap.remove(key);
        if (mHolder != null) {
            return mHolder.getValue();
        } else {
            return null;
        }
    }

    /**
     * Creates a daemon thread to take care of the clean up process
     */
    private void setupCleanUpProcess() {
        this.mExecutorService.scheduleAtFixedRate(this::cleanUp, this.cleanUpInterval, this.cleanUpInterval, TimeUnit.MILLISECONDS);
    }

    private void cleanUp() {
        long now = System.currentTimeMillis();
        this.lastCleanup.set(now);
        if (!mMap.isEmpty()) {
            log.trace("Cleaning up cache {} with {} entries", name, mMap.size());
            Iterator<Map.Entry<K, Holder<V>>> mIterator = mMap.entrySet().iterator();
            while (mIterator.hasNext()) {
                long expiry = timeToLive + mIterator.next().getValue().lastAccessed;
                if (now > expiry) {
                    mIterator.remove();
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a copy of the map at the point of the method call.
     *
     * @return a snapshot of the cache
     */
    @Override
    public Map<K, V> snapshot() {
        Map<K, V> snapshot = new HashMap<>();
        for (K key : mMap.keySet()) {
            snapshot.put(key, mMap.get(key).peek());
        }
        return snapshot;
    }

    /**
     * Holder class for cache entries to monitor access to the entry.
     */
    public static class Holder<T> {

        private long lastAccessed;
        private final T value;

        private Holder(T value) {
            lastAccessed = System.currentTimeMillis();
            this.value = value;
        }

        private T getValue() {
            lastAccessed = System.currentTimeMillis();
            return this.value;
        }

        private T peek() {
            return this.value;
        }

    }

}