package me.mouamle.bot.pdf.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserDataService<K, V> implements SnapshotProvider<String, Integer> {

    private final String name;
    private final int maxEntries;
    private final ConcurrentHashMap<K, Queue<V>> dataMap = new ConcurrentHashMap<>();

    public UserDataService(String name, int maxEntries) {
        this.name = name;
        this.maxEntries = maxEntries;
    }

    public boolean add(K key, V value) {
        Queue<V> images = dataMap.getOrDefault(key, new ConcurrentLinkedQueue<>());
        if (images.size() + 1 > maxEntries) {
            return false;
        }
        images.add(value);
        dataMap.put(key, images);
        return true;
    }

    public boolean addAll(K key, List<V> values) {
        Queue<V> images = dataMap.getOrDefault(key, new ConcurrentLinkedQueue<>());
        if (images.size() + values.size() > maxEntries) {
            return false;
        }
        images.addAll(values);
        dataMap.put(key, images);
        return true;
    }

    public Queue<V> get(K key) {
        return dataMap.getOrDefault(key, new ConcurrentLinkedQueue<>());
    }

    public boolean contains(K key) {
        return dataMap.containsKey(key);
    }

    public boolean isEmpty(K key) {
        return get(key).isEmpty();
    }

    public int size(K key) {
        return get(key).size();
    }

    public void clearUserImages(K key) {
        dataMap.remove(key);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Integer> snapshot() {
        return Collections.singletonMap("total", dataMap.size());
    }
}
