package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.enhanced.entity.RenderEntity;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Box;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CutterEntity extends RenderEntity {

    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE), new BlendingAttribute(true, 1));
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public final CutterScript cutterScript;

    public CutterEntity(float width, float height, float length, Model cutter) {
        this(new Box(width, height, length, MATERIAL, ATTRIBUTES), cutter);
    }

    public CutterEntity(Render render, Model cutter) {
        super(render);
        setName("Cutter");
        this.render.setActive(false);
        cutterScript = addComponent(new CutterScript());
        cutterScript.cutter = cutter;
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
