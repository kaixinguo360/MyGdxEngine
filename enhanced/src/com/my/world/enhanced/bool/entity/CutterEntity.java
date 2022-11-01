package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.model.Box;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CutterEntity extends EnhancedEntity {

    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE), new BlendingAttribute(true, 1));
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public final ModelRender render;
    public final RigidBody rigidBody;
    public final CutterScript cutterScript;

    public CutterEntity(float width, float height, float length) {
        this(width, height, length, null);
    }

    public CutterEntity(float width, float height, float length, Matrix4 offset) {
        setName("Cutter");
        this.render = addComponent(new Box(width, height, length, MATERIAL, ATTRIBUTES));
        this.rigidBody = addComponent(new BoxBody(new Vector3(width / 2, height / 2, length / 2), 0f));
        this.rigidBody.isTrigger = true;
        this.rigidBody.isEnableCallback = true;
        cutterScript = addComponent(new CutterScript());
        cutterScript.cutter = this.render;
        if (offset != null) cutterScript.offset.set(offset);
        cutterScript.type = BooleanUtil.Type.UNION;
    }
}
