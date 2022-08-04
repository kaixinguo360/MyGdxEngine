package com.my.world.enhanced.portal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Cylinder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CylinderDepthMaskEntity extends EnhancedEntity {

    public final Render maskRender;
    public final Render displayRender;
    public final DepthMaskScript depthMaskScript;

    public CylinderDepthMaskEntity(float radius, float height) {
        setName("DepthMask");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.GREEN));
        maskRender = addComponent(new Cylinder(2 * radius, height, 2 * radius, 16, material, VertexAttributes.Usage.Position));
        displayRender = addComponent(new HollowCylinder(radius, height, 16, material, VertexAttributes.Usage.Position));
        depthMaskScript = addComponent(new DepthMaskScript());
        depthMaskScript.addMaskRender(maskRender, position);
    }
}
