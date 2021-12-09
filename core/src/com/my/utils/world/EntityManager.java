package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityManager implements Disposable {

    @Getter
    private final Map<String, Entity> entities = new HashMap<>();

    @Getter
    private final Batch batch = new Batch();

    public Entity addEntity(Entity entity) {
        String id = entity.getId();
        if (entities.containsKey(id)) throw new RuntimeException("Duplicate Entity: " + id);
        entities.put(id, entity);
        return entity;
    }
    public Entity removeEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        return entities.remove(id);
    }
    public Entity getEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        return entities.get(id);
    }

    @Override
    public void dispose() {
        for (Entity entity : entities.values()) {
            entity.dispose();
        }
        entities.clear();
    }

    public class Batch {

        private final Set<Entity> toAdd = new HashSet<>();
        private final Set<String> toRemove = new HashSet<>();

        private Batch() {}

        public Entity addEntity(Entity entity) {
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
                    entities.put(entity.getId(), entity);
                }
                toAdd.clear();
            }
            if (!toRemove.isEmpty()) {
                for (String id : toRemove) {
                    entities.remove(id);
                }
                toRemove.clear();
            }
        }
    }
}
