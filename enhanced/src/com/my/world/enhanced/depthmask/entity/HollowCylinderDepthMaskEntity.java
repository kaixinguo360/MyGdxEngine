package com.my.world.enhanced.depthmask.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.enhanced.depthmask.DepthMaskEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Cylinder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class HollowCylinderDepthMaskEntity extends DepthMaskEntity {

    public final Render maskRender;
    public final Render displayRender;

    public HollowCylinderDepthMaskEntity(float radius, float height) {
        setName("HollowCylinder");
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.GREEN));
        maskRender = addComponent(new Cylinder(2 * radius, height, 2 * radius, 16, material, VertexAttributes.Usage.Position));
        displayRender = addComponent(new HollowCylinder(radius, height, 16, material, VertexAttributes.Usage.Position));
        depthMaskScript.addMaskRender(maskRender, position);
    }
}
