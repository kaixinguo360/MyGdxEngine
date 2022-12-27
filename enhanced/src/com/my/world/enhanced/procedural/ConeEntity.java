package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.rigidbody.ConeBody;
import com.my.world.module.render.model.Cone;

public class ConeEntity extends RigidBodyEntity {

    public static final int DIVISIONS = 16;

    public final float radius;
    public final float height;
    public final int divisions;
    public final Material material;
    public final long attributes;
    public final float mass;

    public ConeEntity(float radius, float height, int divisions, Material material, long attributes, float mass) {
        super(
                new Cone(radius, height, radius, divisions, material, attributes),
                new ConeBody(radius, height, mass)
        );
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        this.mass = mass;
        setName("Cone");
    }

    // ----- Builder ----- //

    public static ConeEntity cone(float radius, float height, PhysicsMaterial material) {
        return cone(radius, height, DIVISIONS, material);
    }

    public static ConeEntity cone(float radius, float height, int divisions, PhysicsMaterial material) {
        ConeEntity entity = new ConeEntity(
                radius, height, divisions,
                material.material, material.attributes, material.getMass((float) (Math.PI * radius * radius * height * (1 / 3)))
        );
        material.config(entity.rigidBody.body);
        return entity;
    }
}
