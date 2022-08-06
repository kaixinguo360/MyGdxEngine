package com.my.world.enhanced.builder;

import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;

public class BuilderManager {

    private final Map<Class<? extends EntityBuilder>, EntityBuilder> builders = new HashMap<>();

    public void scanPackage(String scanPackage) {
        try {
            System.out.println("Scan package: " + scanPackage);
            Reflections reflections = new Reflections(scanPackage);
            Set<Class<? extends EntityBuilder>> types = reflections.getSubTypesOf(EntityBuilder.class);
            for (Class<? extends EntityBuilder> type : types) {
                int modifiers = type.getModifiers();
                if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) continue;
                System.out.println("Add builder: " + type.getName());
                builders.put(type, type.newInstance());
            }
            System.out.println("Scan completed");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public BuilderManager init(Engine engine) {
        Scene scene = engine.getSceneManager().newScene("prefab");

        ArrayList<EntityBuilder> builders = new ArrayList<>(this.builders.values());
        int lastSize = -1;
        while (true) {
            Iterator<EntityBuilder> it = builders.iterator();
            while (it.hasNext()) {
                try {
                    EntityBuilder builder = it.next();
                    builder.init(engine, scene);
                    it.remove();
                } catch (DependenciesException ignored) {
                }
            }
            if (builders.size() == 0) {
                break;
            } else {
                if (builders.size() == lastSize) {
                    throw new RuntimeException("Find circular dependencies: " + builders);
                } else {
                    lastSize = builders.size();
                }
            }
        }

        engine.getSceneManager().removeScene(scene.getId());
        return this;
    }

    public Instance newInstance(Scene scene) {
        return new Instance(scene);
    }

    public class Instance {

        private final Scene scene;

        private Instance(Scene scene) {
            this.scene = scene;
        }

        public Entity build(Class<? extends EntityBuilder> builderType) {
            return build(builderType, null);
        }

        public Entity build(Class<? extends EntityBuilder> builderType, Map<String, Object> params) {
            EntityBuilder builder = builders.get(builderType);
            if (builder == null) throw new RuntimeException("No such builder: " + builderType);
            return builder.build(scene, params);
        }
    }
}
