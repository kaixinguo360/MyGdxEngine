package com.my.world.enhanced.portal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.portal.render.PortalRenderScript;
import com.my.world.enhanced.portal.transfer.PortalTransferScript;
import com.my.world.enhanced.portal.transfer.UniversalPortalTransferScript;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Sphere;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PortalEntity extends EnhancedEntity {

    public final Render render;
    public final RigidBody rigidBody;
    public final Portal portal;
    public final PortalRenderScript renderScript;
    public final PortalTransferScript transferScript;

    public PortalEntity(float radius) {
        setName("Portal");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.RED));
        render = addComponent(new Sphere(2 * radius, 2 * radius, 2 * radius, 16, 16, material, VertexAttributes.Usage.Position));
        rigidBody = addComponent(new SphereBody(radius, 500f));
        rigidBody.isTrigger = true;
        rigidBody.isEnableCallback = true;
        portal = addComponent(new Portal());
        portal.targetTransform = new Matrix4();
        renderScript = addComponent(new PortalRenderScript());
        transferScript = addComponent(new UniversalPortalTransferScript());
    }
}
