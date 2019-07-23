package com.my.utils.world.sys;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Id;
import com.my.utils.world.com.Serialization;

import java.util.HashMap;
import java.util.Map;

public class SerializationSystem extends BaseSystem {

    private static final Json JSON = new Json();
    private static final Map<String, Values> jsonMap = new HashMap<>();

    // ----- Check ----- //
    public boolean check(Entity entity) {
        return entity.contain(Id.class, Serialization.class);
    }

    // ----- Custom ----- //
    public String serialize(String group) {
        jsonMap.clear();
        for (Entity entity : entities) {
            Serialization s = entity.get(Serialization.class);

            if (group == null || group.equals(s.group)) {
                Serialization.Serializer serializer = Serialization.getSerializer(s.serializerId);
                String data = serializer.serialize(entity);
                if (data != null) {
                    Values values = new Values();
                    values.put("id", entity.get(Id.class).id);
                    values.put("group", s.group);
                    values.put("serializerId", s.serializerId);
                    values.put("action", "update");
                    values.put("data", data);
                    jsonMap.put(entity.get(Id.class).id, values);
                }
            }
        }
        for (String removedId : removedIds) {
            Values values = new Values();
            values.put("id", removedId);
            values.put("action", "remove");
            jsonMap.put(removedId, values);
        }
        return JSON.toJson(jsonMap);
    }
    public void deserialize(String json) {
        // ----- Deserialize Json ----- //
        Map<String, Values> jsonMap;
        try {
            jsonMap = JSON.fromJson(HashMap.class, json);
        } catch (SerializationException e) {
            e.printStackTrace();
            return;
        }
        // ----- Update & Remove ----- //
        for (Entity entity : entities) {
            Serialization s = entity.get(Serialization.class);
            Values values = jsonMap.remove(entity.get(Id.class).id);
            if (values != null) {
                Serialization.Serializer serializer = Serialization.getSerializer(s.serializerId);
                String action = values.get("action");
                if ("update".equals(action)) {
                    // Update
                    serializer.update(entity, values.get("data"));
                } else if ("remove".equals(action)) {
                    // Remove
                    serializer.remove(entity);
                }
            }
        }
        // ----- Add ----- //
        for (Values values : jsonMap.values()) {
            Serialization.Serializer serializer = Serialization.getSerializer(values.get("serializerId"));
            String action = values.get("action");
            if ("add".equals(action) || "update".equals(action)) {
                // Add
                serializer.add(values.get("id"), values.get("group"), values.get("serializerId"), values.get("data"));
            }
        }
    }
    private Array<String> removedIds = new Array<>();
    public void remove(String id) {
        removedIds.add(id);
    }

    @Override
    public void dispose() {

    }

    static class Values implements Json.Serializable {

        HashMap<String, String> values = new HashMap<>();
        private void put(String key, String value) {
            values.put(key, value);
        }
        private String get(String key) {
            return values.get(key);
        }

        public void write(Json json) {
            json.writeValue("values", values);
        }

        public void read(Json json, JsonValue jsonData) {
            values = json.readValue("values", HashMap.class, jsonData);
        }
    }
}


