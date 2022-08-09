package com.my.world.enhanced.depthmask.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.enhanced.depthmask.DepthMaskEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class HoleSpaceEntity extends DepthMaskEntity {

    public static final int DIVISIONS = 16;
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE));
    public static final Matrix4 tmpM = new Matrix4();
    public static final float PADDING = 0.002f;

    public final Render maskRender;
    public final Render insideDisplayRender;
    public final Render outsideDisplayRender;

    public HoleSpaceEntity(float Radius, float Depth) {
        setName("HoleSpace");
        maskRender = addComponent(new ProceduralModelRender() {{
            float radius = Radius;
            float depth = Depth;
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("maskRender", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);
            part.setVertexTransform(tmpM.setToTranslation(0, -depth / 2, 0));
            CylinderShapeBuilder.build(part, radius * 2, depth, radius * 2, DIVISIONS, 0, 360, false);
            EllipseShapeBuilder.build(part, radius * 2, radius * 2, 0, 0, DIVISIONS, 0, -depth / 2, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 0, 360f);
            model = mdBuilder.end();
            init();
        }});
        insideDisplayRender = addComponent(new ProceduralModelRender() {{
            float radius = Radius - PADDING;
            float depth = Depth - PADDING;
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("displayRender", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);
            part.setVertexTransform(tmpM.setToTranslation(0, -depth / 2, 0));
            CylinderShapeBuilder.build(part, radius * 2, depth, radius * 2, DIVISIONS, 180f, -180f, false);
            EllipseShapeBuilder.build(part, radius * 2, radius * 2, 0, 0, DIVISIONS, 0, -depth / 2, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 180f, -180f);
            model = mdBuilder.end();
            init();
        }});
        outsideDisplayRender = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("ellipse", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);
            EllipseShapeBuilder.build(part, Radius * 2, Radius * 2, 0, 0, DIVISIONS, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 180f, -180f);
            model = mdBuilder.end();
            init();
        }});
        depthMaskScript.addMaskRender(maskRender, position);
        depthMaskScript.addHiddenRender(insideDisplayRender, position);
    }
}
