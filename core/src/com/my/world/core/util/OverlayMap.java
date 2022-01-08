package com.my.world.core.util;

import java.util.*;

public class OverlayMap<K, V> implements Map<K, V>, Disposable {

    protected String root;
    protected Map<K, V> base;
    protected Map<String, Object> overlay;

    protected boolean disposed = false;
    protected final List<Disposable> linkedObject = new ArrayList<>();

    private OverlayMap() {}

    protected void init(Map<K, V> base, Map<String, Object> overlay, String root) {
        this.base = base;
        this.overlay = overlay;
        this.root = root;
        this.linkedObject.clear();
    }

    @Override
    public void dispose() {
        if (!disposed) {
            this.base = null;
            this.overlay = null;
            this.root = null;
            Disposable.disposeAll(linkedObject);
            pool.free(this);
        }
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

    public Object mergeValue(Object originalValue, Map<String, Object> overlay, String hash) {
        if (overlay.containsKey(hash)) {
            return overlay.get(hash);
        } else {
            if (originalValue instanceof Map) {
                OverlayMap instance = OverlayMap.obtain((Map) originalValue, overlay, hash);
                this.linkedObject.add(instance);
                return instance;
            } else if (originalValue instanceof List){
                OverlayList instance = OverlayList.obtain((List) originalValue, overlay, hash);
                this.linkedObject.add(instance);
                return instance;
            } else {
                return originalValue;
            }
        }
    }

    private static final Pool<OverlayMap> pool = new Pool<>(OverlayMap::new);
    public static <K, V> OverlayMap<K, V> obtain(Map<K, V> base, Map<String, Object> overlay, String root) {
        OverlayMap<K, V> obj = (OverlayMap<K, V>) pool.obtain();
        obj.init(base, overlay, root);
        return obj;
    }
}
