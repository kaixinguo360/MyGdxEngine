package com.my.world.gdx;

import com.my.world.core.Context;

import java.io.*;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaNativeSerializer implements com.my.world.core.Serializer {

    public static final Base64.Encoder encoder = Base64.getEncoder();
    public static final Base64.Decoder decoder = Base64.getDecoder();

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        try {
            Map<String, Object> map = (Map<String, Object>) config;
            String base64 = (String) map.get("serializable");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoder.decode(base64));
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error occurred while deserialize this config: " + config, e);
        }
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            String base64 = encoder.encodeToString(byteArrayOutputStream.toByteArray());
            return (E) new LinkedHashMap<String, Object>() {{
                put("type", obj.getClass().getName());
                put("serializable", base64);
            }};
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while serialize this value: " + obj, e);
        }
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Serializable.class.isAssignableFrom(targetType);
    }
}
