package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.render.model.Cylinder;

public class CylinderEntity extends RigidBodyEntity {

    public static final int DIVISIONS = 16;

    public final float radius;
    public final float height;
    public final int divisions;
    public final Material material;
    public final long attributes;
    public final float mass;

    public CylinderEntity(float radius, float height, int divisions, Material material, long attributes, float mass) {
        super(
                new Cylinder(radius * 2, height, radius * 2, divisions, material, attributes),
                new CylinderBody(new Vector3(radius * 2, height, radius * 2).scl(0.5f), mass)
        );
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        this.mass = mass;
        setName("Cylinder");
    }

    // ----- Builder ----- //

    public static CylinderEntity cylinder(float radius, float height, PhysicsMaterial material) {
        return cylinder(radius, height, DIVISIONS, material);
    }

    public static CylinderEntity cylinder(float radius, float height, int divisions, PhysicsMaterial material) {
        CylinderEntity entity = new CylinderEntity(
                radius, height, divisions,
                material.material, material.attributes, material.getMass((float) (Math.PI * radius * radius * height))
        );
        material.config(entity.rigidBody.body);
        return entity;
    }
}
