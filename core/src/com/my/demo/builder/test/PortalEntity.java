package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.enhanced.EnhancedEntity;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PortalEntity extends EnhancedEntity {


    public final Render render;
    public final RigidBody rigidBody;
    public final Collision collision;
    public final PortalScript portalScript;

    public PortalEntity(float radius) {
        setName("Portal");
        int Divisions = 16;
        long Attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        Material Material = new Material(PBRColorAttribute.createDiffuse(Color.RED));
        float Radius = radius;
        render = addComponent(new ProceduralModelRender() {{
            mdBuilder.begin();
            MeshPartBuilder part = mdBuilder.part("ellipse", GL20.GL_TRIANGLES, Attributes, Material);
            Matrix4 tmpM = Matrix4Pool.obtain();
            part.setVertexTransform(tmpM.rotate(Vector3.X, 90));
            Matrix4Pool.free(tmpM);
            EllipseShapeBuilder.build(part, Radius * 2, Radius * 2, 0, 0, Divisions, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 0, 360f);
            EllipseShapeBuilder.build(part, Radius * 2, Radius * 2, 0, 0, Divisions, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 180f, -180f);
            model = mdBuilder.end();
            init();
        }});
        rigidBody = addComponent(new SphereBody(radius, 500f));
        rigidBody.isTrigger = true;
        collision = addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        portalScript = addComponent(new PortalScript());
        portalScript.targetTransform = new Matrix4();
    }
}
