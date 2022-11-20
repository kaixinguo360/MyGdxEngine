package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.ModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class RigidBodyCutterEntity extends DetectorEntity {

    protected static final ModelBuilder mdBuilder = new ModelBuilder();
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.BLUE), new BlendingAttribute(true, 1));
    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public final ModelRender render;
    public final CutterScript cutterScript;

    public RigidBodyCutterEntity(Model cutterModel, RigidBody rigidBody) {
        super(rigidBody);
        setName("RigidBodyCutterEntity");
        render = addComponent(new ModelRender(cutterModel));
        cutterScript = addComponent(new CutterScript());
        cutterScript.cutter = cutterModel;
        cutterScript.type = BooleanUtil.Type.UNION;
    }

    public static RigidBodyCutterEntity sphere(float radius, int divisionsU, int divisionsV) {
        return new RigidBodyCutterEntity(
                mdBuilder.createSphere(radius, radius, radius, divisionsU, divisionsV, MATERIAL, ATTRIBUTES),
                new SphereBody(radius, 0, true)
        );
    }
}
