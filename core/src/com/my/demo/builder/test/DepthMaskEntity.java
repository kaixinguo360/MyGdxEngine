package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.enhanced.EnhancedEntity;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Cylinder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class DepthMaskEntity extends EnhancedEntity {

    public final Render maskRender;
    public final Render displayRender;
    public final RigidBody rigidBody;
    public final Collision collision;
    public final DepthMaskScript depthMaskScript;

    public DepthMaskEntity(float radius, float height) {
        setName("DepthMask");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.GREEN));
        maskRender = addComponent(new Cylinder(2 * radius, height, 2 * radius, 16, material, VertexAttributes.Usage.Position));
        displayRender = addComponent(new HollowCylinder(radius, height, 16, material, VertexAttributes.Usage.Position));
        rigidBody = addComponent(new CylinderBody(new Vector3(radius, radius, radius), 500f));
        rigidBody.isTrigger = true;
        collision = addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        depthMaskScript = addComponent(new CollisionDepthMaskScript());
    }
}
