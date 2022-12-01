package com.my.world.core.util;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Configurable;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class Lambdas {

    public static final Base64.Encoder encoder = Base64.getEncoder();
    public static final Base64.Decoder decoder = Base64.getDecoder();

    public static Class<?> getType(Object value) {
        return Lambdas.isSerializableLambda(value) ? Configurable.class : value.getClass();
    }

    public static boolean isSerializableLambda(Object value) {
        try {
            Class<?> type = value.getClass();
            if (!type.isSynthetic()) return false;
            type.getDeclaredMethod("writeReplace");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isSerializedLambdaConfig(Object config) {
        return config instanceof Map && ((Map<?, ?>) config).containsKey("lambda");
    }

    public static Map<String, Object> dump(Object value) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            String base64 = encoder.encodeToString(byteArrayOutputStream.toByteArray());
            return new LinkedHashMap<String, Object>() {{
                put("type", value.getClass().getName());
                put("lambda", base64);
            }};
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while serialize this value: " + value, e);
        }
    }

    public static Object load(Object config) {
        try {
            Map<String, Object> map = (Map<String, Object>) config;
            String base64 = (String) map.get("lambda");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoder.decode(base64));
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error occurred while deserialize this config: " + config, e);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        Vector3 input = new Vector3();

        Runnable runnable = (Runnable & Serializable) () -> System.out.println(input);
        Method method = runnable.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) method.invoke(runnable);
        System.out.println("serializedLambda = " + serializedLambda);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(runnable);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Runnable target = (Runnable) objectInputStream.readObject();

        System.out.println("target = " + target);
        target.run();
    }
}
