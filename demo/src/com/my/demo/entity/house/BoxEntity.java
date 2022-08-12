package com.my.demo.entity.house;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.function.Supplier;

public class BoxEntity extends EnhancedEntity {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE));

    public final ProceduralModelRender render;
    public final BoxBody rigidBody;

    public final float width;
    public final float height;
    public final float length;
    public final float density;
    public final Material Material;
    public final long Attributes;
    public final float volume;
    public final float mass;

    public BoxEntity(EnhancedContext context) {
        setName("Box");

        width = context.get("BoxWidth", Float.class, 1f);
        height = context.get("BoxHeight", Float.class, 1f);
        length = context.get("BoxLength", Float.class, 1f);
        density = context.get("BoxDensity", Float.class, 0f);
        Material = context.get("BoxMaterial", Material.class, MATERIAL);
        Attributes = context.get("BoxAttributes", Long.class, ATTRIBUTES);
        volume = width * height * length;
        mass = volume * density;

        render = addComponent(new ProceduralModelRender(){{
            model = mdBuilder.createBox(width, height, length, Material, Attributes);
            init();
        }});
        rigidBody = addComponent(new BoxBody(new Vector3(width / 2, height / 2, length / 2), mass));
    }

    public static <T> T getReturn(Supplier<T> supplier) {
        return supplier.get();
    }
}
