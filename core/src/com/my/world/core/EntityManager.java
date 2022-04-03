package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;

import java.util.*;

public class EntityManager implements Disposable {

    public static final String CONTEXT_ENTITY_PROVIDER = "ENTITY_PROVIDER";
    public static final String CONTEXT_ENTITY_ADDER = "ENTITY_ADDER";
    public static final String CONTEXT_ENTITY_PREFIX = "ENTITY_PREFIX";

    @Getter
    private final Map<String, Entity> entities = new LinkedHashMap<>();

    @Getter
    private final Map<EntityFilter, Set<Entity>> filters = new LinkedHashMap<>();

    @Getter
    private final Map<EntityFilter, EntityListener> listeners = new HashMap<>();

    @Getter
    private final Batch batch = new Batch();

    // ---- Entity ---- //
    public <T extends Entity> T addEntity(T entity) {
        if (entity.getId() == null) entity.setId(entity.getName() + "_" + UUID.randomUUID());
        String id = entity.getId();
        if (entities.containsKey(id)) throw new EntityManagerException("Duplicate Entity: id=" + id);
        entities.put(id, entity);
        return entity;
    }
    public Entity removeEntity(String id) {
        if (!entities.containsKey(id)) throw new EntityManagerException("No Such Entity: id=" + id);
        Entity removed = entities.remove(id);
        for (Map.Entry<EntityFilter, Set<Entity>> entry : filters.entrySet()) {
            EntityFilter filter = entry.getKey();
            Set<Entity> entities = entry.getValue();
            if (entities.contains(removed)) {
                entities.remove(removed);
                if (listeners.containsKey(filter)) listeners.get(filter).afterEntityRemoved(removed);
            }
        }
        return removed;
    }
    public Entity findEntityById(String id) {
        if (!entities.containsKey(id)) throw new EntityManagerException("No Such Entity: id=" + id);
        return entities.get(id);
    }
    public Entity findEntityByName(String name) {
        if (name == null) throw new EntityManagerException("Name of entity can not be null");
        for (Entity entity : entities.values()) {
            if (name.equals(entity.getName())) {
                return entity;
            }
        }
        throw new EntityManagerException("No Such Entity: name=" + name);
    }

    // ---- Filter ---- //
    public void addFilter(EntityFilter entityFilter) {
        if (filters.containsKey(entityFilter)) throw new EntityManagerException("Duplicate Entity Filter: " + entityFilter);
        filters.put(entityFilter, new HashSet<>());
    }
    public void removeFilter(EntityFilter entityFilter) {
        if (!filters.containsKey(entityFilter)) throw new EntityManagerException("No Such Entity Filter: " + entityFilter);
        filters.get(entityFilter).clear();
        filters.remove(entityFilter);
    }
    public Collection<Entity> getEntitiesByFilter(EntityFilter entityFilter) {
        if (!filters.containsKey(entityFilter)) throw new EntityManagerException("No Such Entity Filter: " + entityFilter);
        return filters.get(entityFilter);
    }
    public void updateFilters() {
        for (Entity entity : entities.values()) {
            if (entity.isChanged()) {
                entity.setChanged(false);
                for (Map.Entry<EntityFilter, Set<Entity>> entry : filters.entrySet()) {
                    EntityFilter filter = entry.getKey();
                    Set<Entity> entities = entry.getValue();
                    if (entity.isActiveInHierarchy() && filter.filter(entity)) {
                        if (!entities.contains(entity)) {
                            entities.add(entity);
                            if (listeners.containsKey(filter)) listeners.get(filter).afterEntityAdded(entity);
                        }
                    } else {
                        if (entities.contains(entity)){
                            entities.remove(entity);
                            if (listeners.containsKey(filter)) listeners.get(filter).afterEntityRemoved(entity);
                        }
                    }
                }
            }
        }
    }

    // ---- Listener ---- //
    public void addListener(EntityFilter entityFilter, EntityListener entityListener) {
        if (listeners.containsKey(entityFilter)) throw new EntityManagerException("Duplicate Entity Listener Of This Filter: " + entityFilter);
        if (!filters.containsKey(entityFilter)) addFilter(entityFilter);
        listeners.put(entityFilter, entityListener);
    }
    public void removeListener(EntityFilter entityFilter, EntityListener entityListener) {
        if (!listeners.containsKey(entityFilter)) throw new EntityManagerException("No Such Entity Listener Of This Filter: " + entityFilter);
        listeners.remove(entityFilter);
    }

    @Override
    public void dispose() {
        clearEntity();

        for (Set<Entity> entitySet : filters.values()) {
            entitySet.clear();
        }
        filters.clear();
        listeners.clear();

        batch.toAdd.clear();
        batch.toRemove.clear();
    }
    public void clearEntity() {
        List<Entity> entityList = new ArrayList<>(entities.values());
        for (int i = entityList.size() - 1; i >= 0; i--) {
            removeEntity(entityList.get(i).getId());
        }

        Disposable.disposeAll(entityList);
        Disposable.disposeAll(entities);
    }

    public class Batch {

        private final Set<Entity> toAdd = new HashSet<>();
        private final Set<String> toRemove = new HashSet<>();

        private Batch() {}

        public <T extends Entity> T addEntity(T entity) {
            String id = entity.getId();
            if (entities.containsKey(id)) throw new EntityManagerException("Duplicate Entity: id=" + id);
            toAdd.add(entity);
            return entity;
        }
        public Entity removeEntity(String id) {
            if (!entities.containsKey(id)) throw new EntityManagerException("No Such Entity: id=" + id);
            toRemove.add(id);
            return entities.get(id);
        }

        public <T extends Entity> T addAll(T entity, boolean resetId) {
            if (resetId) entity.setId(null);
            addEntity(entity);
            if (!entity.getChildren().isEmpty()) {
                for (Entity child : entity.getChildren()) {
                    addAll(child, resetId);
                }
            }
            return entity;
        }
        public Entity removeAll(String id) {
            if (!entities.containsKey(id)) throw new EntityManagerException("No Such Entity: id=" + id);
            Entity entity = entities.get(id);
            if (!entity.getChildren().isEmpty()) {
                for (Entity child : entity.getChildren()) {
                    removeAll(child.getId());
                }
            }
            removeEntity(id);
            return entity;
        }

        public void commit() {
            if (!toAdd.isEmpty()) {
                for (Entity entity : toAdd) {
                    EntityManager.this.addEntity(entity);
                }
                toAdd.clear();
            }
            if (!toRemove.isEmpty()) {
                for (String id : toRemove) {
                    EntityManager.this.removeEntity(id);
                }
                toRemove.clear();
            }
        }
    }
    
    public static class EntityManagerException extends RuntimeException {
        private EntityManagerException(String message) {
            super(message);
        }
    }

}
