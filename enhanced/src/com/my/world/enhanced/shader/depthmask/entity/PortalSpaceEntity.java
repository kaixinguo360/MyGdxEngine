package com.my.world.enhanced.shader.depthmask.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.enhanced.shader.depthmask.DepthMaskEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PortalSpaceEntity extends DepthMaskEntity {

    public static final int DIVISIONS = 16;
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE));
    public static final Matrix4 tmpM = new Matrix4();

    public final Render maskRender;
    public final Render displayRender;

    public PortalSpaceEntity(float Radius, float Depth) {
        setName("PortalSpace");
        maskRender = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("cylinder", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);
            part.setVertexTransform(tmpM.setToTranslation(0, -Depth / 2, 0));
            CylinderShapeBuilder.build(part, Radius * 2, Depth, Radius * 2, DIVISIONS);
            model = mdBuilder.end();
            init();
        }});
        displayRender = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("ellipse", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);
            EllipseShapeBuilder.build(part, Radius * 2, Radius * 2, 0, 0, DIVISIONS, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 360);
            EllipseShapeBuilder.build(part, Radius * 2, Radius * 2, 0, 0, DIVISIONS, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 180f, -180f);
            model = mdBuilder.end();
            init();
        }});
        depthMaskScript.addMaskRender(maskRender, position);
    }
}
