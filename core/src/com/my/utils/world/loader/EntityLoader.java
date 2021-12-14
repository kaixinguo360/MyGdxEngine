package com.my.utils.world.loader;

import com.my.utils.world.Component;
import com.my.utils.world.Entity;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config Example:
 * <pre>
 *     id: Entity_1
 *     components:
 *       - type: com.my.com.ComponentType1
 *         config: ...
 *       - type: com.my.com.ComponentType2
 *         config: ...
 * </pre>
 */
public class EntityLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        Map<String, Object> map = (Map<String, Object>) config;
        Entity entity = new Entity();

        // Process ID
        String entityId = (String) map.get("id");
        entity.setId(entityId);

        // Process Components
        List<Map<String, Object>> components = (List<Map<String, Object>>) map.get("components");
        if (components != null) {
            try {
                for (Map<String, Object> component : components) {
                    String componentTypeName = (String) component.get("type");
                    Object componentConfig = component.get("config");
                    Class<? extends Component> componentType = (Class<? extends Component>) Class.forName(componentTypeName);
                    entity.addComponent(context.getLoaderManager().load(componentConfig, componentType, context));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("No such class error: " + e.getMessage(), e);
            }
        }

        return (T) entity;
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        Map<String, Object> map = new HashMap<>();
        Entity entity = (Entity) obj;

        // Process ID
        String entityId = entity.getId();
        map.put("id", entityId);

        // Process Components
        List<Map<String, Object>> components = new ArrayList<>();
        for (Component component : entity.getComponents().values()) {
            String componentTypeName = component.getClass().getName();
            Object componentConfig = context.getLoaderManager().getConfig(component, Map.class, context);
            Map<String, Object> componentMap = new HashMap<>();
            componentMap.put("type", componentTypeName);
            componentMap.put("config", componentConfig);
            components.add(componentMap);
        }
        if (components.size() > 0) {
            map.put("components", components);
        }

        return (E) map;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (Entity.class.isAssignableFrom(targetType));
    }
}
