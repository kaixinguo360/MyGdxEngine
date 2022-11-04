package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Box;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CutterEntity extends RigidBodyEntity {

    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE), new BlendingAttribute(true, 1));
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public final CutterScript cutterScript;

    public CutterEntity(float width, float height, float length, Model cutter) {
        this(width, height, length, cutter, null);
    }

    public CutterEntity(float width, float height, float length, Model cutter, Matrix4 offset) {
        this(
                new Box(width, height, length, MATERIAL, ATTRIBUTES),
                new BoxBody(new Vector3(width / 2, height / 2, length / 2), 0f),
                cutter,
                offset
        );
    }

    public CutterEntity(Render render, RigidBody rigidBody, Model cutter) {
        this(render, rigidBody, cutter, null);
    }

    public CutterEntity(Render render, RigidBody rigidBody, Model cutter, Matrix4 offset) {
        super(render, rigidBody);
        setName("Cutter");
        this.render.setActive(false);
        this.rigidBody.isTrigger = true;
        this.rigidBody.isKinematic = true;
        this.rigidBody.isEnableCallback = true;
        cutterScript = addComponent(new CutterScript());
        cutterScript.cutter = cutter;
        if (offset != null) cutterScript.offset.set(offset);
        cutterScript.type = BooleanUtil.Type.UNION;
    }

    protected static final BoundingBox tmpB = new BoundingBox();
    protected static final Vector3 tmpV = new Vector3();

    public static CutterEntity get(Model cutter) {
        cutter.calculateBoundingBox(tmpB);
        tmpB.getDimensions(tmpV);
        return new CutterEntity(tmpV.x, tmpV.y, tmpV.z, cutter);
    }
}
