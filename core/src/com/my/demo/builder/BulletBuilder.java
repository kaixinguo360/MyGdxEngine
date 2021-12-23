package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.BombScript;
import com.my.demo.script.CutterScript;
import com.my.demo.script.ExplosionScript;
import com.my.demo.script.RemoveScript;
import com.my.utils.bool.BooleanCutUtils;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.Capsule;

import static com.my.demo.builder.SceneBuilder.attributes;

public class BulletBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("bullet", ModelRender.class, new Capsule(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", ModelRender.class, new Capsule(0.5f, 2, 8, Color.GRAY, attributes));

        assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
        assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));

        // ----- Init Boolean Cursor ----- //
        Color color = new Color(0, 0, 0, 0.5f);
        assetsManager.addAsset("cursor1", ModelRender.class, new Box(0.1f, 0.1f, 2, color, attributes));
        assetsManager.addAsset("cursor2", ModelRender.class, new Box(0.2f, 0.2f, 2, color, attributes));
        assetsManager.addAsset("cursor3", ModelRender.class, new Box(1, 1, 1, color, attributes));
        assetsManager.addAsset("cursor4", ModelRender.class, new Box(100, 100, 100, color, attributes));
        assetsManager.addAsset("cursor5", ModelRender.class, new Box(0.05f, 100, 100, color, attributes));

        assetsManager.getAsset("cursor1", ModelRender.class).model.nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        assetsManager.getAsset("cursor2", ModelRender.class).model.nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        assetsManager.getAsset("cursor3", ModelRender.class).model.nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        assetsManager.getAsset("cursor4", ModelRender.class).model.nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        assetsManager.getAsset("cursor5", ModelRender.class).model.nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;

        assetsManager.addAsset("cursor1", TemplateRigidBody.class, new BoxBody(new Vector3(0.05f, 0.05f, 1), 50f));
        assetsManager.addAsset("cursor2", TemplateRigidBody.class, new BoxBody(new Vector3(0.1f, 0.1f, 1), 50f));
        assetsManager.addAsset("cursor3", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f, 0.5f, 5), 0f));
        assetsManager.addAsset("cursor4", TemplateRigidBody.class, new BoxBody(new Vector3(50, 50, 500), 0f));
        assetsManager.addAsset("cursor5", TemplateRigidBody.class, new BoxBody(new Vector3(0.025f, 50, 500), 0f));

        scene.createPrefab(BulletBuilder::createExplosion);
        scene.createPrefab(BulletBuilder::createCutter);
        scene.createPrefab(BulletBuilder::createBomb);
        scene.createPrefab(BulletBuilder::createBullet);
        scene.createPrefab(BulletBuilder::createCutterBomb);
    }

    public static String createExplosion(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = scene.getAsset("sphere", TemplateRigidBody.class);
        entity.addComponent(new PresetTemplateRigidBody(templateRigidBody, true));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new ExplosionScript());
        scene.addEntity(entity);
        return "Explosion";
    }

    public static String createCutter(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Cutter");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new PresetModelRender(scene.getAsset("cursor5", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("cursor5", TemplateRigidBody.class), true));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        CutterScript cutterScript = entity.addComponent(new CutterScript());
        cutterScript.cutter = scene.getAsset("cursor4", ModelRender.class);
        cutterScript.offset = new Matrix4().translate(50, 0, 0);
        cutterScript.type = BooleanCutUtils.Type.BOTH;
        scene.addEntity(entity);
        return "Cutter";
    }

    public static String createBullet(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bullet");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new PresetModelRender(scene.getAsset("bullet", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bullet", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        scene.addEntity(entity);
        return "Bullet";
    }

    public static String createBomb(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bomb");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new PresetModelRender(scene.getAsset("bomb", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bomb", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = scene.getAsset("Explosion", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        scene.addEntity(entity);
        return "Bomb";
    }

    public static String createCutterBomb(Scene scene) {
        Entity entity = new Entity();
        entity.setName("CutterBomb");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new PresetModelRender(scene.getAsset("bomb", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bomb", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = scene.getAsset("Cutter", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        scene.addEntity(entity);
        return "CutterBomb";
    }
}
