package com.my.demo.entity.house.door;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.my.world.enhanced.depthmask.DepthMaskEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Cylinder;
import com.my.world.module.render.model.ProceduralModelRender;

public class CircleHoleEntity extends DepthMaskEntity {

    public final Render maskRender;
    public final Render displayRender;

    public CircleHoleEntity(float Radius, float Height, Material Material, long Attributes) {
        setName("CircleHole");
        maskRender = addComponent(new Cylinder(2 * Radius, Height, 2 * Radius, 16, Material, Attributes));
        displayRender = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("part", GL20.GL_TRIANGLES, Attributes, Material);
            CylinderShapeBuilder.build(part, Radius * 2, Height, Radius * 2, 16, 180f, -180f, false);
            model = mdBuilder.end();
            init();
        }});
        depthMaskScript.addMaskRender(maskRender, position);
    }
}
