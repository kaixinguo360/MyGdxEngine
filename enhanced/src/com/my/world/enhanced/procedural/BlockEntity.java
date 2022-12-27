package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.Box;

public class BlockEntity extends RigidBodyEntity {

    public final float width;
    public final float height;
    public final float depth;
    public final Material material;
    public final long attributes;
    public final float mass;

    public BlockEntity(float width, float height, float depth, Material material, long attributes, float mass) {
        super(
                new Box(width, height, depth, material, attributes),
                new BoxBody(new Vector3(width, height, depth).scl(0.5f), mass)
        );
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.material = material;
        this.attributes = attributes;
        this.mass = mass;
        setName("Block");
    }

    // ----- Builder ----- //

    public static BlockEntity block(float size, PhysicsMaterial material) {
        return block(size, size, size, material);
    }

    public static BlockEntity block(float width, float height, float depth, PhysicsMaterial material) {
        BlockEntity entity = new BlockEntity(
                width, height, depth,
                material.material, material.attributes, material.getMass(width * height * depth)
        );
        material.config(entity.rigidBody.body);
        return entity;
    }
}
