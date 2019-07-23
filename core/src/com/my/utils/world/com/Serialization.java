package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Entity;

import java.util.HashMap;
import java.util.Map;

public class Serialization implements Component {

    public String group;
    public String serializerId;


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
