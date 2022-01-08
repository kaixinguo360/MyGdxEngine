package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.game.script.GunController;
import com.my.game.script.GunScript;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.physics.script.ConstraintController;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.Cylinder;

public class GunBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("barrel", ModelRender.class, new Box(1, 1, 5, Color.GREEN, attributes));
        assetsManager.addAsset("gunRotate", ModelRender.class, new Cylinder(1, 1, 1, 8, Color.CYAN, attributes));

        assetsManager.addAsset("barrel", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f,0.5f,2.5f), 5f));
        assetsManager.addAsset("gunRotate", TemplateRigidBody.class, new CylinderBody(new Vector3(0.5f,0.5f,0.5f), 50f));
    }

    public static String createGun(Scene scene) {

        // Gun Entity
        Entity entity = new Entity();
        entity.setName("Gun");
        entity.addComponent(new Position(new Matrix4()));
        GunScript gunScript = entity.addComponent(new GunScript());
        gunScript.bulletPrefab = getAsset(scene, "Bullet", Prefab.class);
        gunScript.bombPrefab = getAsset(scene, "Bomb", Prefab.class);
        addEntity(scene, entity);

        Entity rotate_Y = createRotate(scene, "rotate_Y", new Matrix4().translate(0, 0.5f, 0), new GunController(), null);
        Entity rotate_X = createRotate(scene, "rotate_X", new Matrix4().translate(0, 1.5f, 0).rotate(Vector3.Z, 90), new GunController(-90, 0), rotate_Y);
        Entity barrel = createBarrel(scene, "barrel", new Matrix4().translate(0, 1.5f, -3), rotate_X);

        rotate_Y.setParent(entity);
        rotate_X.setParent(entity);
        barrel.setParent(entity);

        return "Gun";
    }

    private static Entity createBarrel(Scene scene, String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity(scene, "barrel");
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(scene, name, transform, entity);
    }

    private static Entity createRotate(Scene scene, String name, Matrix4 transform, ConstraintController controller, Entity base) {
        Matrix4 relTransform = (base == null) ? new Matrix4().inv().mul(transform) :
                new Matrix4(base.getComponent(Position.class).getLocalTransform()).inv().mul(transform);
        Entity entity = createEntity(scene, "gunRotate");
        if (base != null) {
            entity.addComponent(
                    new HingeConstraint(
                            base,
                            relTransform.rotate(Vector3.X, 90),
                            new Matrix4().rotate(Vector3.X, 90),
                            false
                    )
            );
        }
        entity.addComponent(controller);
        return addEntity(scene, name, transform, entity);
    }
}
