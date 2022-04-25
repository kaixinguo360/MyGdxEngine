package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.my.demo.builder.enhanced.EnhancedEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Sphere;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PortalEntity extends EnhancedEntity {

    public final Render render;
    public final PortalScript portalScript;

    public PortalEntity(float radius) {
        setName("Portal");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.RED));
        render = addComponent(new Sphere(2 * radius, 2 * radius, 2 * radius, 16, 16, material, VertexAttributes.Usage.Position));
        portalScript = addComponent(new PortalScript());
    }
}
