package com.my.world.enhanced.builder;

import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityRegister {

    private final List<Class<? extends Entity>> entities = new ArrayList<>();

    public void scanPackage(String scanPackage) {
        System.out.println("Scan package: " + scanPackage);
        Reflections reflections = new Reflections(scanPackage);
        Set<Class<? extends EnhancedEntity>> types = reflections.getSubTypesOf(EnhancedEntity.class);
        for (Class<?> type : types) {
            if (!Entity.class.isAssignableFrom(type)) {
                throw new RuntimeException("Not a entity: " + type);
            }
            int modifiers = type.getModifiers();
            if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) continue;
            System.out.println("Register entity: " + type.getName());
            entities.add((Class<? extends Entity>) type);
        }
        System.out.println("Scan completed");
    }

    public EntityRegister init(Engine engine) {
        Scene scene = engine.getSceneManager().newScene("prefab");

        ArrayList<Class<? extends Entity>> entities = new ArrayList<>(this.entities);
        int lastSize = -1;
        while (true) {
            Iterator<Class<? extends Entity>> it = entities.iterator();
            while (it.hasNext()) {
                try {
                    Class<? extends Entity> type = it.next();
                    Method method = type.getMethod("init", Engine.class, Scene.class);
                    method.invoke(null, engine, scene);
                    it.remove();
                } catch (NoSuchMethodException ignored) {
                    it.remove();
                } catch (InvocationTargetException | IllegalAccessException e) {
                    if (!(e.getCause() instanceof DependenciesException)) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (entities.size() == 0) {
                break;
            } else {
                if (entities.size() == lastSize) {
                    throw new RuntimeException("Find circular dependencies: " + entities);
                } else {
                    lastSize = entities.size();
                }
            }
        }

        engine.getSceneManager().removeScene(scene.getId());
        return this;
    }
}
