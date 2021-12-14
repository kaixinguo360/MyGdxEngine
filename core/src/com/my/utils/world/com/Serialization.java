package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Entity;
import com.my.utils.world.LoadContext;
import com.my.utils.world.LoadableResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Serialization implements Component, LoadableResource {

    public String group;
    public String serializerId;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        this.group = (String) config.get("group");
        this.serializerId = (String) config.get("serializerId");
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        return new HashMap<String, Object>() {{
            put("group", group);
            put("serializerId", serializerId);
        }};
    }

    // ----- Serializer ----- //
    public interface Serializer {
        String serialize(Entity entity);
        void add(String id, String group, String serializerId, String data);
        void update(Entity entity, String data);
        void remove(Entity entity);
    }
    private final static Map<String, Serializer> serializers = new HashMap<>();
    public static void addSerializer(String id, Serializer serializer) {
        serializers.put(id, serializer);
    }
    public static void removeSerializer(String id) {
        serializers.remove(id);
    }
    public static Serializer getSerializer(String id) {
        if (!serializers.containsKey(id)) throw new RuntimeException("No Such Serializer: " + id);
        return serializers.get(id);
    }
}
