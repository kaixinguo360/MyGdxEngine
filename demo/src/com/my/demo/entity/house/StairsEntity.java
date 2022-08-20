package com.my.demo.entity.house;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class StairsEntity extends EnhancedEntity {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createBaseColorFactor(Color.WHITE));
    public static final Material BOUNDING_BOX_MATERIAL = new Material(PBRColorAttribute.createBaseColorFactor(new Color(1, 1, 1, 0.2f)));

    public final float stairsHeight;
    public final float stairsWidth;
    public final float stairsLength;
    public final float stairsStepsSize;
    public final float stairsPlatformSize;
    public final float stairsPlatformThickness;

    public final ProceduralModelRender render;
    public final TemplateRigidBody rigidBody;

    public final float singleStairsWidth;
    public final float singleStairsX;
    public final float singleStairsZ;

    public StairsEntity(EnhancedContext context) {
        setName("Stairs");

        {
            stairsHeight = context.get("楼梯高度", Float.class, 3.5f);
            stairsWidth = context.get("楼梯宽度", Float.class, 3f);
            stairsLength = context.get("楼梯长度", Float.class, 9f);
            stairsStepsSize = context.get("楼梯台阶宽度", Float.class, 0.3f);
            stairsPlatformSize = context.get("楼梯平台宽度", Float.class, 1.5f);
            stairsPlatformThickness = context.get("楼梯平台厚度", Float.class, 0.4f);
        }

        singleStairsWidth = stairsWidth / 2;
        singleStairsX = singleStairsWidth / 2;
        singleStairsZ = (stairsLength - stairsPlatformSize * 2) / 2;

        float platformZ = stairsLength / 2 - stairsPlatformSize / 2;

        render = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("stairs", GL20.GL_TRIANGLES, ATTRIBUTES, MATERIAL);

            buildSingleStairs(part, -singleStairsX, -singleStairsZ, 0, singleStairsZ, stairsHeight / 2);
            buildSingleStairs(part, singleStairsX, singleStairsZ, stairsHeight / 2, -singleStairsZ, stairsHeight);

            buildBox(
                    part,
                    stairsWidth, stairsPlatformThickness, stairsPlatformSize,
                    0, 0, -platformZ
            );
            buildBox(
                    part,
                    stairsWidth, stairsPlatformThickness, stairsPlatformSize,
                    0, stairsHeight / 2, platformZ
            );

//            MeshPartBuilder boundingBox = mdBuilder.part("boundingBox", GL20.GL_TRIANGLES, ATTRIBUTES, BOUNDING_BOX_MATERIAL);
//            buildBox(
//                    boundingBox,
//                    stairsWidth, stairsHeight, stairsLength,
//                    0, stairsHeight / 2, 0
//            );

            model = mdBuilder.end();
            init();
        }});

        rigidBody = addComponent(new TemplateRigidBody(Bullet.obtainStaticNodeShape(render.modelInstance.nodes), 0));
    }

    private void buildSingleStairs(MeshPartBuilder part, float x, float startZ, float startY, float endZ, float endY) {
        int sgn = (endZ > startZ) ? 1 : -1;
        float wholeLength = (endZ - startZ) * sgn;
        float wholeHeight = endY - startY;
        int stepNum = (int) (wholeLength / stairsStepsSize);
        float stepLength = wholeLength / stepNum;
        float stepHeight = wholeHeight / stepNum;

        for (int i = 0; i < stepNum; i++) {
            float stepZ = startZ + stepLength * i * sgn;
            float stepY = startY + stepHeight * i;
            buildBox(
                    part,
                    singleStairsWidth, stepHeight, stepLength,
                    x, stepY, stepZ
            );
        }
    }

    private void buildBox(MeshPartBuilder part, float width, float height, float depth, float centerX, float centerY, float centerZ) {
        part.setVertexTransform(new Matrix4().setToTranslation(centerX, centerY, centerZ));
        BoxShapeBuilder.build(part, width, height, depth);
    }
}
