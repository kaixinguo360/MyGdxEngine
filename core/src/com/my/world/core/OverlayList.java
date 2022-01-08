package com.my.world.core;

import java.util.*;

public class OverlayList<E> implements List<E>, Disposable {

    protected String root;
    protected List<E> base;
    protected Map<String, Object> overlay;

    protected boolean disposed = false;
    protected final List<Disposable> linkedObject = new ArrayList<>();

    private OverlayList() {}

    protected void init(List<E> base, Map<String, Object> overlay, String root) {
        this.disposed = false;
        this.linkedObject.clear();
        this.base = base;
        this.overlay = overlay;
        this.root = root;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            this.disposed = true;
            Disposable.disposeAll(linkedObject);
            this.base = null;
            this.overlay = null;
            this.root = null;
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
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new OverlayIterator(0);
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size()];
        for (int i = 0; i < size(); i++) {
            objects[i] = get(i);
        }
        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
        java.lang.System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        if (index >= base.size()) return null;
        return (E) mergeValue(base.get(index), overlay, root + "[" + index + "]");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new OverlayIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new OverlayIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public class OverlayIterator implements ListIterator<E> {

        protected int index;

        public OverlayIterator(int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            E obj = get(index);
            index++;
            return obj;
        }

        @Override
        public boolean hasPrevious() {
            return index >= 0;
        }

        @Override
        public E previous() {
            index--;
            return get(index);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
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

    private static final Pool<OverlayList> pool = new Pool<>(OverlayList::new);
    public static <E> OverlayList<E> obtain(List<E> base, Map<String, Object> overlay, String root) {
        OverlayList<E> obj = (OverlayList<E>) pool.obtain();
        obj.init(base, overlay, root);
        return obj;
    }
}
