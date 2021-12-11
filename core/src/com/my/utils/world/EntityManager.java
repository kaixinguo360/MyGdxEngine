package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.*;

public class EntityManager implements Disposable {

    @Getter
    private final Map<String, Entity> entities = new HashMap<>();

    @Getter
    private final Map<EntityFilter, Set<Entity>> filters = new HashMap<>();

    @Getter
    private final Map<EntityFilter, EntityListener> listeners = new HashMap<>();

    @Getter
    private final Batch batch = new Batch();

    private final World world;

    public EntityManager(World world) {
        this.world = world;
    }

    // ---- Entity ---- //
    public <T extends Entity> T addEntity(T entity) {
        String id = entity.getId();
        if (entities.containsKey(id)) throw new RuntimeException("Duplicate Entity: " + id);
        entities.put(id, entity);
        if (entity instanceof AfterAdded) ((AfterAdded) entity).afterAdded(world);
        return entity;
    }
    public Entity removeEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        Entity removed = entities.remove(id);
        for (Map.Entry<EntityFilter, Set<Entity>> entry : filters.entrySet()) {
            EntityFilter filter = entry.getKey();
            entry.getValue().remove(removed);
            if (listeners.containsKey(filter)) listeners.get(filter).afterRemoved(removed);
        }
        if (removed instanceof AfterRemoved) ((AfterRemoved) removed).afterRemoved(world);
        return removed;
    }
    public Entity getEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        return entities.get(id);
    }

    // ---- Filter ---- //
    public void addFilter(EntityFilter entityFilter) {
        if (filters.containsKey(entityFilter)) throw new RuntimeException("Duplicate Entity Filter: " + entityFilter);
        filters.put(entityFilter, new HashSet<>());
    }
    public void removeFilter(EntityFilter entityFilter) {
        if (!filters.containsKey(entityFilter)) throw new RuntimeException("No Such Entity Filter: " + entityFilter);
        filters.get(entityFilter).clear();
        filters.remove(entityFilter);
    }
    public Collection<? extends Entity> getEntitiesByFilter(EntityFilter entityFilter) {
        if (!filters.containsKey(entityFilter)) throw new RuntimeException("No Such Entity Filter: " + entityFilter);
        return filters.get(entityFilter);
    }
    public void updateFilters() {
        for (Entity entity : entities.values()) {
            if (!entity.isHandled()) {
                entity.setHandled(true);
                for (Map.Entry<EntityFilter, Set<Entity>> entry : filters.entrySet()) {
                    EntityFilter filter = entry.getKey();
                    Set<Entity> entities = entry.getValue();
                    if (filter.filter(entity)) {
                        entities.add(entity);
                        if (listeners.containsKey(filter)) listeners.get(filter).afterAdded(entity);
                    } else {
                        entities.remove(entity);
                        if (listeners.containsKey(filter)) listeners.get(filter).afterRemoved(entity);
                    }
                }
            }
        }
    }

    // ---- Listener ---- //
    public void addListener(EntityFilter entityFilter, EntityListener entityListener) {
        if (listeners.containsKey(entityFilter)) throw new RuntimeException("Duplicate Entity Listener Of This Filter: " + entityFilter);
        if (!filters.containsKey(entityFilter)) addFilter(entityFilter);
        listeners.put(entityFilter, entityListener);
    }
    public void removeListener(EntityFilter entityFilter, EntityListener entityListener) {
        if (!listeners.containsKey(entityFilter)) throw new RuntimeException("No Such Entity Listener Of This Filter: " + entityFilter);
        listeners.remove(entityFilter);
    }

    @Override
    public void dispose() {
        for (Entity entity : entities.values()) {
            if (entity instanceof Disposable) {
                ((Disposable) entity).dispose();
            }
        }
        entities.clear();
    }

    public class Batch {

        private final Set<Entity> toAdd = new HashSet<>();
        private final Set<String> toRemove = new HashSet<>();

        private Batch() {}

        public <T extends Entity> T addEntity(T entity) {
            String id = entity.getId();
            if (entities.containsKey(id)) throw new RuntimeException("Duplicate Entity: " + id);
            toAdd.add(entity);
            return entity;
        }
        public Entity removeEntity(String id) {
            if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
            toRemove.add(id);
            return entities.get(id);
        }

        public void commit() {
            if (!toAdd.isEmpty()) {
                for (Entity entity : toAdd) {
                    addEntity(entity);
                }
                toAdd.clear();
            }
            if (!toRemove.isEmpty()) {
                for (String id : toRemove) {
                    removeEntity(id);
                }
                toRemove.clear();
            }
        }
    }

}
