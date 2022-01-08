package com.my.world.core;

import lombok.Getter;

import java.util.*;

public class OverlayMap<K, V> implements Map<K, V> {

    @Getter
    protected final String root;

    @Getter
    protected final Map<K, V> base;

    @Getter
    protected final Map<String, Object> overlay;

    public OverlayMap(Map<K, V> base, Map<String, Object> overlay) {
        this(base, overlay, "");
    }

    public OverlayMap(Map<K, V> base, Map<String, Object> overlay, String root) {
        this.base = base;
        this.overlay = overlay;
        this.root = root;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (!base.containsKey(key)) return null;
        return (V) mergeValue(base.get(key), overlay, root + "." + key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return base.keySet();
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (Entry<K, V> entry : base.entrySet()) {
            K key = entry.getKey();
            V value = get(key);
            values.add(value);
        }
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<K> keys = new HashSet<>();
        Set<Entry<K, V>> entries = new LinkedHashSet<>();
        for (Entry<K, V> entry : base.entrySet()) {
            K key = entry.getKey();
            V value = get(key);
            entries.add(new OverlayEntry(key, value));
        }
        return Collections.unmodifiableSet(entries);
    }

    public static class OverlayEntry<K, V> implements Entry<K, V> {

        protected K key;

        protected V value;

        public OverlayEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    public static Object mergeValue(Object originalValue, Map<String, Object> overlay, String hash) {
        if (overlay.containsKey(hash)) {
            return overlay.get(hash);
        } else {
            if (originalValue instanceof Map) {
                return new OverlayMap((Map) originalValue, overlay, hash);
            } else if (originalValue instanceof List){
                return new OverlayList((List) originalValue, overlay, hash);
            } else {
                return originalValue;
            }
        }
    }
}
