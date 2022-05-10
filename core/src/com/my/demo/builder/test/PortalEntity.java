package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.my.demo.builder.enhanced.EnhancedEntity;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.Render;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PortalEntity extends EnhancedEntity {

    public final Render render;
    public final RigidBody rigidBody;
    public final Collision collision;
    public final PortalScript portalScript;

    public PortalEntity(float radius) {
        setName("Portal");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.RED));
        render = addComponent(new PortalModelRender(2 * radius, 2 * radius, material, VertexAttributes.Usage.Position));
        rigidBody = addComponent(new SphereBody(radius, 500f));
        rigidBody.isTrigger = true;
        collision = addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        portalScript = addComponent(new PortalScript());
        portalScript.targetTransform = new Matrix4();
    }
}
