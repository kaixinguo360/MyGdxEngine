package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.com.Id;

import java.util.HashMap;
import java.util.Map;

public class World implements Disposable {

    // ----- System ----- //
    private final Map<Class, System> systems = new HashMap<>();
    public <T extends System> T addSystem(Class<T> type, T system) {
        if (systems.containsKey(type)) throw new RuntimeException("Duplicate System: " + type);
        systems.put(type, system);
        return system;
    }
    public <T extends System> T removeSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.remove(type);
    }
    public <T extends System> T getSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.get(type);
    }

    // ----- Entity ----- //
    private final Map<String, Entity> entities = new HashMap<>();
    public Entity addEntity(String id, Entity entity) {
        if (entities.containsKey(id)) throw new RuntimeException("Duplicate Entity: " + id);
        entity.add(Id.class, new Id(id));
        entities.put(id, entity);
        return entity;
    }
    public Entity removeEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        Entity entity = entities.remove(id);
        entity.remove(Id.class);
        return entity;
    }
    public Entity getEntity(String id) {
        if (!entities.containsKey(id)) throw new RuntimeException("No Such Entity: " + id);
        return entities.get(id);
    }

    // ----- Update ----- //
    public void update() {
        for (Map.Entry<Class, System> entry : systems.entrySet()) {
            Class type = entry.getKey();
            System system = entry.getValue();
            Array<Entity> sortEntities = system.getEntities();
            sortEntities.clear();
            for (Entity entity : entities.values()) {
                if (system.check(entity)) {
                    sortEntities.add(entity);
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (System system : systems.values()) {
            system.dispose();
        }
        systems.clear();
        entities.clear();
    }
}
