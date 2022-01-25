package com.my.world.core;

import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializerManager {

    public static final String CONTEXT_FIELD_NAME = "SERIALIZER_MANAGER";

    private final Engine engine;

    @Getter
    protected final List<Serializer> serializers = new ArrayList<>();
    private final Map<String, Serializer> serializerCache = new HashMap<>();
    private final Map<String, Serializer.Setter> setterCache = new HashMap<>();

    SerializerManager(Engine engine) {
        this.engine = engine;
    }

    public <E, T> T load(E config, Class<T> type, Context context) {
        if (config == null) {
            throw new RuntimeException("No such serializer: null -> " + type);
        }
        String hash = config.getClass() + " -> " + type;
        if (serializerCache.containsKey(hash)) {
            return serializerCache.get(hash).load(config, type, context);
        } else {
            for (Serializer serializer : serializers) {
                if (serializer.canSerialize(config.getClass(), type)) {
                    serializerCache.put(hash, serializer);
                    return serializer.load(config, type, context);
                }
            }
        }
        throw new RuntimeException("No such serializer: " + config.getClass() + " -> " + type);
    }

    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        String hash = configType + " -> " + obj.getClass();
        if (serializerCache.containsKey(hash)) {
            return serializerCache.get(hash).dump(obj, configType, context);
        } else {
            for (Serializer serializer : serializers) {
                if (serializer.canSerialize(configType, obj.getClass())) {
                    serializerCache.put(hash, serializer);
                    return serializer.dump(obj, configType, context);
                }
            }
        }
        throw new RuntimeException("No such serializer to get config: " + configType + " -> " + obj.getClass());
    }

    public void set(Object source, Object target) {
        Class<?> sourceType = source.getClass();
        Class<?> targetType = target.getClass();
        String hash = sourceType + " -> " + targetType;
        if (setterCache.containsKey(hash)) {
            setterCache.get(hash).set(source, target);
            return;
        } else {
            for (Serializer serializer : serializers) {
                if (serializer instanceof Serializer.Setter) {
                    Serializer.Setter setter = (Serializer.Setter) serializer;
                    if (setter.canSet(sourceType, targetType)) {
                        setterCache.put(hash, setter);
                        setter.set(source, target);
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("No such setter: " + source + " -> " + target);
    }

    public <T extends Serializer> T findSerializer(Class<T> type) {
        for (Serializer serializer : serializers) {
            if (type.isInstance(serializer)) {
                return (T) serializer;
            }
        }
        return null;
    }

    public <T extends Serializer.Setter> T findSetter(Class<T> type) {
        for (Serializer serializer : serializers) {
            if (serializer instanceof Serializer.Setter) {
                Serializer.Setter setter = (Serializer.Setter) serializer;
                if (type.isInstance(setter)) {
                    return (T) setter;
                }
            }
        }
        return null;
    }

    // ----- File Utils ----- //

    public static String readFile(String path) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String str;
            String ls = java.lang.System.getProperty("line.separator");
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append(ls);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(String content, String path) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(content);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
