package com.my.world.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.my.world.core.Context;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFlagAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.badlogic.gdx.graphics.g3d.Attribute.getAttributeAlias;
import static com.badlogic.gdx.graphics.g3d.Attribute.getAttributeType;

public class GdxNativeSerializer implements com.my.world.core.Serializer {

    public static final Json json = new Json();

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        Map<String, Object> map = (Map<String, Object>) config;
        String jsonStr = (String) map.get("json");
        return json.fromJson(type, jsonStr);
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        String jsonStr = json.toJson(obj);
        return (E) new LinkedHashMap<String, Object>() {{
            put("type", obj.getClass().getName());
            put("json", jsonStr);
        }};
    }

    public static final Set<Class<?>> canHandledTypes = new HashSet<Class<?>>() {{
        add(Material.class);
        add(ParticleEffect.class);
    }};

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && canHandledTypes.contains(targetType);
    }

    // ----- AttributeSerializer ----- //

    static {
        // Gdx Attributes
        json.setSerializer(DepthTestAttribute.class, (AttributeSerializer<DepthTestAttribute>) (json, jsonData, type) -> new DepthTestAttribute(
                readAttributeType(json, jsonData),
                json.readValue("depthFunc", Integer.class, jsonData),
                json.readValue("depthRangeNear", Float.class, jsonData),
                json.readValue("depthRangeFar", Float.class, jsonData),
                json.readValue("depthMask", Boolean.class, jsonData)
        ));
        json.setSerializer(IntAttribute.class, (AttributeSerializer<IntAttribute>) (json, jsonData, type) -> new IntAttribute(
                readAttributeType(json, jsonData),
                json.readValue("value", Integer.class, jsonData)
        ));
        json.setSerializer(FloatAttribute.class, (AttributeSerializer<FloatAttribute>) (json, jsonData, type) -> new FloatAttribute(
                readAttributeType(json, jsonData),
                json.readValue("value", Float.class, jsonData)
        ));
        json.setSerializer(ColorAttribute.class, (AttributeSerializer<ColorAttribute>) (json, jsonData, type) -> new ColorAttribute(
                readAttributeType(json, jsonData),
                json.readValue("color", Color.class, jsonData)
        ));
        // GLTF Attributes
        json.setSerializer(FogAttribute.class, (AttributeSerializer<FogAttribute>) (json, jsonData, type) ->
                (FogAttribute) new FogAttribute(readAttributeType(json, jsonData))
                        .set(json.readValue("value", Vector3.class, jsonData)));
        json.setSerializer(PBRColorAttribute.class, (AttributeSerializer<PBRColorAttribute>) (json, jsonData, type) -> new PBRColorAttribute(
                readAttributeType(json, jsonData),
                json.readValue("color", Color.class, jsonData)
        ));
        json.setSerializer(PBRFlagAttribute.class, (AttributeSerializer<PBRFlagAttribute>) (json, jsonData, type) -> new PBRFlagAttribute(
                readAttributeType(json, jsonData)
        ));
        json.setSerializer(PBRFloatAttribute.class, (AttributeSerializer<PBRFloatAttribute>) (json, jsonData, type) -> new PBRFloatAttribute(
                readAttributeType(json, jsonData),
                json.readValue("value", Float.class, jsonData)
        ));
    }

    protected interface AttributeSerializer<T extends Attribute> extends Json.Serializer<T> {
        default void write(Json json, T attribute, Class knownType) {
            json.writeObjectStart(attribute.getClass(), knownType);
            json.writeFields(attribute);
            writeAttributeType(json, attribute.type);
            json.writeObjectEnd();
        }
    }

    protected static long readAttributeType(Json json, JsonValue jsonData) {
        return getAttributeType(json.readValue("typeAlias", String.class, jsonData));
    }

    protected static void writeAttributeType(Json json, long attributeType) {
        json.writeValue("typeAlias", getAttributeAlias(attributeType));
    }
}
