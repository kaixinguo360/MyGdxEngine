package com.my.world.enhanced.depthmask.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.my.world.enhanced.depthmask.DepthMaskAttribute;
import com.my.world.enhanced.render.RenderOrderAttribute;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.ProceduralModelRender;

public class EnhancedHoleSpaceEntity extends HoleSpaceEntity {

    public final Material displayMaskRenderMaterial;
    public final Render displayMaskRender;

    public EnhancedHoleSpaceEntity(float Radius, float Depth) {
        super(Radius, Depth);
        setName("EnhancedHoleSpace");
        displayMaskRenderMaterial = new Material(
                new DepthMaskAttribute(new Color(1, 1, 1, 0.5f)),
                new RenderOrderAttribute(1)
        );
        displayMaskRender = addComponent(new ProceduralModelRender() {{
            float radius = Radius - PADDING * 0.8f;
            float depth = Depth - PADDING * 0.8f;
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("cylinder", GL20.GL_TRIANGLES, ATTRIBUTES, displayMaskRenderMaterial);
            part.setVertexTransform(tmpM.setToTranslation(0, -depth / 2, 0));
            CylinderShapeBuilder.build(part, radius * 2, depth, radius * 2, DIVISIONS);
            model = mdBuilder.end();
            init();
        }});
        depthMaskScript.addHiddenRender(displayMaskRender, position);
        depthMaskScript.material.set(new RenderOrderAttribute(-1));
    }
}
