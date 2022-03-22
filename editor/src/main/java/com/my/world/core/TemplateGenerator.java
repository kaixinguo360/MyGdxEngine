package com.my.world.core;

import com.alibaba.fastjson.JSON;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.my.world.module.common.Position;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.render.Render;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TemplateGenerator {

    public static void main(String[] args) {

        Reflections reflections = new Reflections("com.my.world");
        Set<Class<? extends Component>> types = reflections.getSubTypesOf(Component.class);

        List<Map<String, Object>> componentConfigs = new ArrayList<>();

        for (Class<? extends Component> type : types) {
            int modifiers = type.getModifiers();
            if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
                continue;
            }
            if (type == Position.class || type == RigidBody.class || type == Render.class) {
                continue;
            }

            componentConfigs.add(new HashMap<String, Object>() {{

                Component newInstance = null;
                try {
                    newInstance = type.newInstance();
                } catch (InstantiationException | IllegalAccessException ignored) {}
                Component instance = newInstance;

                put("type", type.getName());
                put("config", new HashMap<String, Object>() {{
                    List<Field> fields = getFields(type);
                    for (Field field : fields) {
                        field.setAccessible(true);
                        put(field.getName(), new HashMap<String, String>() {{
                            Object obj = null;
                            try {
                                obj = (instance == null) ? null : field.get(instance);
                            } catch (IllegalAccessException ignored) {}
                            put("default", TemplateGenerator.toString(obj));
                            put("type", TemplateGenerator.getName(field.getType()));
                        }});
                    }
                }});
            }});
        }

//        java.lang.System.out.println(new Yaml().dumpAsMap(componentConfigs));
        java.lang.System.out.println(JSON.toJSONString(componentConfigs));
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "None";
        } else if (obj instanceof Entity){
            Entity o = (Entity) obj;
            return String.format("'%s'", o.getId());
        } else if (obj instanceof Collection){
            java.lang.System.out.println("Unsupported type: Collection");
            return String.format("'%s'", obj);
        } else if (obj instanceof Vector3){
            Vector3 o = (Vector3) obj;
            return String.format("'[%s, %s, %s]'", o.x, o.y, o.z);
        } else if (obj instanceof Color){
            Color o = (Color) obj;
            return String.format("'[%s, %s, %s, %s]'", o.r, o.g, o.b, o.a);
        } else if (obj instanceof Quaternion){
            Quaternion o = (Quaternion) obj;
            return String.format("'[%s, %s, %s, %s]'", o.x, o.y, o.z, o.w);
        } else if (obj instanceof Matrix4){
            Matrix4 o = (Matrix4) obj;
            float[] v = o.val;
            return String.format("'[%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s]'",
                    v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12], v[13], v[14], v[15]);
        } else if (obj instanceof Boolean){
            Boolean o = (Boolean) obj;
            return o ? "True" : "False";
        } else if (obj instanceof Number){
            return obj.toString();
        } else {
            return String.format("'%s'", obj);
        }
    }

    public static String getName(Class type) {
        if (type == null) {
            return null;
        } else if (Entity.class.isAssignableFrom(type)){
            return "entity";
        } else if (Collection.class.isAssignableFrom(type)){
            java.lang.System.out.println("Unsupported type: Collection");
            return type.getName();
        } else if (Vector3.class.isAssignableFrom(type)){
            return "vector";
        } else if (Color.class.isAssignableFrom(type)){
            return "color";
        } else if (Quaternion.class.isAssignableFrom(type)){
            return "quaternion";
        } else if (Matrix4.class.isAssignableFrom(type)){
            return "matrix";
        } else {
            return type.getName();
        }
    }

    public static List<Field> getFields(Class<?> type) {
        List<Field> fields;
        fields = new ArrayList<>();
        Class<?> tmpType = type;
        while (tmpType != null && tmpType != Object.class) {
            for (Field field : tmpType.getDeclaredFields()) {
                if (field.isAnnotationPresent(Config.class)) {
                    fields.add(field);
                }
            }
            tmpType = tmpType.getSuperclass();
        }
        return fields;
    }
}
