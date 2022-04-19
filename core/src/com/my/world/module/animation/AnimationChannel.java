package com.my.world.module.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.*;
import com.my.world.module.common.SyncComponent;

import java.lang.System;
import java.lang.reflect.Field;
import java.util.List;

public class AnimationChannel implements Configurable {

    @Config public String entity;
    @Config public Class<? extends Component> component;
    @Config public int index;
    @Config public String field;
    @Config public Curve<?> values;

    private String hash;

    public String getHash() {
        if (hash == null) {
            if (entity == null) {
                hash = '.' + component.getName() + '[' + index + "]." + field;
            } else {
                hash = entity + '.' + component.getName() + '[' + index + "]." + field;
            }
        }
        return hash;
    }

    public void update(float currentTime, float weights, Animation animation) {
        Instance instance = getChannelInstance(animation);

        if (instance == null) return;

        if (instance.component instanceof SyncComponent) {
            animation.changedSyncComponents.add((SyncComponent) instance.component);
        }

        String hash = getHash();
        boolean useBlend = animation.changedChannels.contains(hash);
        animation.changedChannels.add(hash);

        Object value = this.values.valueAt(currentTime);

        Field field = instance.field;
        Class<?> targetType = field.getType();
        if (!isPrimitive(targetType) && !targetType.isInstance(value)) {
            Scene scene = animation.scene;
            Context context = scene.subContext();
            value = scene.getEngine().getSerializerManager().load(value, targetType, context);
            context.dispose();
            if (!targetType.isInstance(value)) {
                throw new RuntimeException("Can not convert this value to this type: " + value + " -> " + field.getType().getName());
            }
        }

        if (weights != 1 && value != null) {
            value = applyWeightsToValue(weights, value);
        }

        if (useBlend) {
            if (value != null) {
                blendValueToInstance(value, instance);
            }
        } else {
            applyValueToInstance(value, instance, animation);
        }
    }

    // ----- Blend Values Utils ----- //

    private void applyValueToInstance(Object value, Instance instance, Animation animation) {
        Field field = instance.field;
        Object target = instance.target;

        Class<?> targetType = field.getType();
        try {
            if (isPrimitive(targetType)) {
                field.set(target, value);
            } else {
                Object currentValue = field.get(target);
                if (currentValue != null) {
                    Scene scene = animation.scene;
                    scene.getEngine().getSerializerManager().set(value, currentValue);
                } else {
                    field.set(target, value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not set this value to this type: " + value + " -> " + field.getType().getName());
        }
    }

    private void blendValueToInstance(Object value, Instance instance) {
        Field field = instance.field;
        Object target = instance.target;

        Class<?> targetType = field.getType();
        try {
            Object currentValue = field.get(target);
            if (isPrimitive(targetType)) {
                field.set(target, blendTwoValues(currentValue, value));
            } else {
                if (currentValue != null) {
                    blendTwoValues(currentValue, value);
                } else {
                    field.set(target, value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not blend this value to this type: " + value + " -> " + field.getType().getName());
        }
    }

    private Object blendTwoValues(Object v1, Object v2) {
        Class<?> type1 = v1.getClass();
        Class<?> type2 = v2.getClass();
        if (type1 != type2 && !type1.isAssignableFrom(type2) && !type2.isAssignableFrom(type1)) {
            throw new RuntimeException("Can not blend these two type: " + type1.getName() + " + " + type2.getName());
        }
        if (isPrimitive(v1.getClass())) {
            if (v1 instanceof Float) {
                return (float) (v1) * (float) (v2);
            } else if (v1 instanceof Integer) {
                return (int) (v1) * (int) (v2);
            } else if (v1 instanceof Long) {
                return (long) (v1) * (long) (v2);
            } else if (v1 instanceof Double) {
                return (double) (v1) * (double) (v2);
            } else if (v1 instanceof Short) {
                return (short) ((short) (v1) * (short) (v2));
            } else if (v1 instanceof Byte) {
                return (byte) ((byte) (v1) * (byte) (v2));
            } else {
                return v1;
            }
        } else if (v1 instanceof Vector3) {
            return ((Vector3) v1).add(((Vector3) v2));
        } else if (v1 instanceof Vector2) {
            return ((Vector2) v1).add(((Vector2) v2));
        } else if (v1 instanceof Matrix4) {
            // TODO Add Matrix4 blend logic
            return v1;
        } else if (v1 instanceof Quaternion) {
            Quaternion q1 = (Quaternion) v1;
            Quaternion q2 = (Quaternion) v2;
            q1.setEulerAngles(
                    q1.getYaw() * q2.getYaw(),
                    q1.getPitch() * q2.getPitch(),
                    q1.getRoll() * q2.getRoll()
            );
            return q1;
        } else if (v1 instanceof Color) {
            Color c1 = (Color) v1;
            Color c2 = (Color) v2;
            return c1.add(c2);
        } else {
            return v1;
        }
    }

    private static Object applyWeightsToValue(float weights, Object value) {
        if (isPrimitive(value.getClass())) {
            if (value instanceof Float) {
                return (float) (value) * weights;
            } else if (value instanceof Integer) {
                return (int) ((int) (value) * weights);
            } else if (value instanceof Long) {
                return (long) ((long) (value) * weights);
            } else if (value instanceof Double) {
                return (double) (value) * weights;
            } else if (value instanceof Short) {
                return (short) ((short) (value) * weights);
            } else if (value instanceof Byte) {
                return (byte) ((byte) (value) * weights);
            } else {
                return value;
            }
        } else if (value instanceof Vector3) {
            return ((Vector3) value).scl(weights);
        } else if (value instanceof Vector2) {
            return ((Vector2) value).scl(weights);
        } else if (value instanceof Matrix4) {
            // TODO Add Matrix4 apply weights logic
            return value;
        } else if (value instanceof Quaternion) {
            Quaternion q = (Quaternion) value;
            q.setEulerAngles(
                    q.getYaw() * weights,
                    q.getPitch() * weights,
                    q.getRoll() * weights
            );
            return q;
        } else if (value instanceof Color) {
            return ((Color) value).mul(weights);
        } else {
            return value;
        }
    }

    // ----- ChannelInstance Utils ----- //

    private Instance getChannelInstance(Animation animation) {
        AnimationChannel channel = this;
        String hash = channel.getHash();
        AnimationChannel.Instance instance;

        if (animation.channelInstanceCaches.containsKey(hash)) {
            return animation.channelInstanceCaches.get(hash);
        }

        Entity entity;
        if (channel.entity == null) {
            entity = animation.entity;
        } else {
            entity = animation.entity.findChildByName(channel.entity);
            if (entity == null) {
                System.out.println("No Such Channel: " + channel);
                animation.channelInstanceCaches.put(hash, null);
                return null;
            }
        }

        try {
            List<? extends Component> components = entity.getComponents(channel.component);
            if (channel.index >= components.size()) {
                System.out.println("No Such Channel: " + channel);
                animation.channelInstanceCaches.put(hash, null);
                return null;
            }
            Component component = components.get(channel.index);

            String[] fieldNames = channel.field.split("\\.");
            Object nextTarget = component;
            Object target = null;
            Field field = null;
            for (String fieldName : fieldNames) {
                target = nextTarget;
                field = getField(target, fieldName);
                if (field == null) {
                    System.out.println("No Such Channel: " + channel);
                    animation.channelInstanceCaches.put(hash, null);
                    return null;
                }
                field.setAccessible(true);
                nextTarget = field.get(target);
            }

            instance = new AnimationChannel.Instance();
            instance.channel = channel;
            instance.component = component;
            instance.target = target;
            instance.field = field;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("Get Channel Error, No Such Channel: " + channel);
            animation.channelInstanceCaches.put(hash, null);
            return null;
        }

        animation.channelInstanceCaches.put(hash, instance);
        return instance;
    }

    // ----- Class Operation Utils ----- //

    private static Field getField(Object obj, String name) {
        Class<?> tmpType = obj.getClass();
        while (tmpType != null && tmpType != Object.class) {
            try {
                return tmpType.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                tmpType = tmpType.getSuperclass();
            }
        }
        return null;
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || Number.class.isAssignableFrom(type) || type == String.class ||
                type == Boolean.class || type == Character.class;
    }

    static class Instance {

        Component component;
        AnimationChannel channel;

        Object target;
        Field field;
    }
}
