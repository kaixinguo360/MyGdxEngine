package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.render.model.Capsule;

public class CapsuleEntity extends RigidBodyEntity {

    public static final int DIVISIONS = 16;

    public final float radius;
    public final float height;
    public final int divisions;
    public final Material material;
    public final long attributes;
    public final float mass;

    public CapsuleEntity(float radius, float height, int divisions, Material material, long attributes, float mass) {
        super(
                new Capsule(radius, height, divisions, material, attributes),
                new CapsuleBody(radius, height, mass)
        );
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        this.mass = mass;
        setName("Capsule");
    }

    // ----- Builder ----- //

    public static CapsuleEntity capsule(float radius, float height, PhysicsMaterial material) {
        return capsule(radius, height, DIVISIONS, material);
    }

    public static CapsuleEntity capsule(float radius, float height, int divisions, PhysicsMaterial material) {
        CapsuleEntity entity = new CapsuleEntity(
                radius, height, divisions,
                material.material, material.attributes, material.getMass((float) (Math.PI * radius * radius * (height + radius * (4 / 3))))
        );
        material.config(entity.rigidBody.body);
        return entity;
    }
}
