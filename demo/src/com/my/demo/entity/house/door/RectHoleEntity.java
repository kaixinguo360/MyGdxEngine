package com.my.demo.entity.house.door;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.depthmask.DepthMaskEntity;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class RectHoleEntity extends DepthMaskEntity {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE));

    public final Render maskRender;
    public final Render displayRender;
    public final RigidBody rigidBody;
    public final Collision collision;
    public final HoleScript holeScript;

    public RectHoleEntity(EnhancedContext context) {
        setName("RectHole");

        float Width = context.get("HoleWidth", Float.class, 2f);
        float Height = context.get("HoleHeight", Float.class, 2f);
        float Depth = context.get("HoleDepth", Float.class, 2f);
        float DetectDepthPadding = context.get("HoleDetectDepthPadding", Float.class, 1f);
        float DetectWidthMargin = context.get("HoleDetectWidthMargin", Float.class, 0.1f);
        float DetectHeightMargin = context.get("HoleDetectHeightMargin", Float.class, 0.1f);
        Material Material = context.get("HoleMaterial", Material.class, MATERIAL);
        long Attributes = context.get("HoleAttributes", Long.class, ATTRIBUTES);

        maskRender = addComponent(new Box(Width, Height, Depth, Material, Attributes));
        displayRender = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("part", GL20.GL_TRIANGLES, Attributes, Material);
            float x = Width / 2;
            float y = Height / 2;
            float z = Depth / 2;
            // Up
            part.rect(
                    x, y, z,
                    -x, y, z,
                    -x, y, -z,
                    x, y, -z,
                    0, -1, 0
            );
            // Down
            part.rect(
                    x, -y, z,
                    x, -y, -z,
                    -x, -y, -z,
                    -x, -y, z,
                    0, 1, 0
            );
            // Left
            part.rect(
                    -x, y, z,
                    -x, -y, z,
                    -x, -y, -z,
                    -x, y, -z,
                    1, 0, 0
            );
            // Right
            part.rect(
                    x, y, z,
                    x, y, -z,
                    x, -y, -z,
                    x, -y, z,
                    -1, 0, 0
            );
            model = mdBuilder.end();
            init();
        }});
        rigidBody = addComponent(new BoxBody(new Vector3((Width - DetectWidthMargin * 2) / 2, (Height - DetectHeightMargin * 2) / 2, (Depth + DetectDepthPadding * 2) / 2), 0));
        rigidBody.isTrigger = true;
        rigidBody.isKinematic = true;
        collision = addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        holeScript = addComponent(new HoleScript());
        depthMaskScript.addMaskRender(maskRender, position);
    }
}
