package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.model.Sphere;

public class SphereEntity extends RigidBodyEntity {

    public static final int DIVISIONS_U = 16;
    public static final int DIVISIONS_V = 16;

    public final float radius;
    public final int divisionsU;
    public final int divisionsV;
    public final Material material;
    public final long attributes;
    public final float mass;

    public SphereEntity(float radius, int divisionsU, int divisionsV, Material material, long attributes, float mass) {
        super(
                new Sphere(radius, radius, radius, divisionsU, divisionsV, material, attributes),
                new SphereBody(radius, mass)
        );
        this.radius = radius;
        this.divisionsU = divisionsU;
        this.divisionsV = divisionsV;
        this.material = material;
        this.attributes = attributes;
        this.mass = mass;
        setName("Sphere");
    }

    // ----- Builder ----- //

    public static SphereEntity sphere(float radius, PhysicsMaterial material) {
        return sphere(radius, DIVISIONS_U, DIVISIONS_V, material);
    }

    public static SphereEntity sphere(float radius, int divisionsU, int divisionsV, PhysicsMaterial material) {
        SphereEntity entity = new SphereEntity(
                radius, divisionsU, divisionsV,
                material.material, material.attributes, material.getMass((float) (Math.PI * radius * radius * radius * (4 / 3)))
        );
        material.config(entity.rigidBody.body);
        return entity;
    }
}
