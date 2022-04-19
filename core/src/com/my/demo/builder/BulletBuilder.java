package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.BombScript;
import com.my.demo.script.ExplosionScript;
import com.my.demo.script.RemoveByPositionScript;
import com.my.demo.script.RemoveByTimeScript;
import com.my.world.core.*;
import com.my.world.module.animation.*;
import com.my.world.module.common.Position;
import com.my.world.module.gltf.light.GLTFPointLight;
import com.my.world.module.gltf.render.GLTFModel;
import com.my.world.module.gltf.render.GLTFModelInstance;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.rigidbody.SphereBody;

import java.util.ArrayList;

public class BulletBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("bullet", GLTFModel.class, new GLTFModel("obj/bullet.gltf"));
        assetsManager.addAsset("bomb", GLTFModel.class, new GLTFModel("obj/bomb.gltf"));

        assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
        assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));

        assetsManager.addAsset("explosionAnimation", Playable.class, createExplosionAnimation());

        scene.createPrefab(BulletBuilder::createExplosion);
        scene.createPrefab(BulletBuilder::createBomb);
        scene.createPrefab(BulletBuilder::createBullet);
    }

    public static AnimationClip createExplosionAnimation() {

        AnimationClip clip = new AnimationClip();

        // Channel - light[0].intensity
        AnimationChannel c1 = new AnimationChannel();
        c1.component = GLTFPointLight.class;
        c1.index = 0;
        c1.field = "light.intensity";
        c1.values = new BezierCurve(new ArrayList<Vector2>(){{
            add(new Vector2(0, 20));
            add(new Vector2(0.1f, 20));

            add(new Vector2(0, 40));
            add(new Vector2(0.1f, 40));
            add(new Vector2(0.15f, 40));

            add(new Vector2(0.15f, 40));
            add(new Vector2(0.2f, 40));
            add(new Vector2(0.25f, 40));

            add(new Vector2(0, 0));
            add(new Vector2(0.5f, 0));
        }});
        clip.channels.add(c1);

        // Channel - light[0].range
        AnimationChannel c2 = new AnimationChannel();
        c2.component = GLTFPointLight.class;
        c2.index = 0;
        c2.field = "light.range";
        c2.values = new BezierCurve(new ArrayList<Vector2>(){{
                add(new Vector2(0, 20f));
                add(new Vector2(0.1f, 20f));

                add(new Vector2(0, 30f));
                add(new Vector2(0.1f, 30f));
                add(new Vector2(0.2f, 30f));

                add(new Vector2(0, 0));
                add(new Vector2(0.4f, 0));
        }});
        clip.channels.add(c2);

        // Channel - light[1].intensity
        AnimationChannel c3 = new AnimationChannel();
        c3.component = GLTFPointLight.class;
        c3.index = 1;
        c3.field = "light.intensity";
        c3.values = new BezierCurve(new ArrayList<Vector2>(){{
                add(new Vector2(0.3f, 40));
                add(new Vector2(0.4f, 40));

                add(new Vector2(-0.5f, 0));
                add(new Vector2(0.5f, 0));
        }});
        clip.channels.add(c3);

        return clip;
    }

    public static String createExplosion(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new RemoveByTimeScript()).maxTime = 0.5f;
        scene.addEntity(entity);

        Entity explosionScriptEntity = new Entity();
        explosionScriptEntity.setParent(entity);
        explosionScriptEntity.setName("Explosion");
        explosionScriptEntity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = scene.getAsset("sphere", TemplateRigidBody.class);
        explosionScriptEntity.addComponent(new PresetTemplateRigidBody(templateRigidBody, true));
        explosionScriptEntity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        explosionScriptEntity.addComponent(new ExplosionScript());
        scene.addEntity(explosionScriptEntity);

        Entity fireworksEffectsEntity = new Entity();
        fireworksEffectsEntity.setParent(entity);
        fireworksEffectsEntity.setName("FireworksEffects");
        fireworksEffectsEntity.addComponent(new Position(new Matrix4()));
        fireworksEffectsEntity.addComponent(new RemoveByTimeScript()).maxTime = 0.5f;
        fireworksEffectsEntity.addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 40f, 60f));
        fireworksEffectsEntity.addComponent(new GLTFPointLight(Color.ORANGE.cpy(), 40f, 30f));
        Animation animation = fireworksEffectsEntity.addComponent(new Animation());
        animation.useLocalTime = true;
        animation.addPlayable("default", scene.getAsset("explosionAnimation", Playable.class));
        scene.addEntity(fireworksEffectsEntity);

        return "Explosion";
    }

    public static String createBullet(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bullet");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("bullet", GLTFModel.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bullet", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveByPositionScript());
        entity.addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 20f, 30f));
        scene.addEntity(entity);
        return "Bullet";
    }

    public static String createBomb(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bomb");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("bomb", GLTFModel.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bomb", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveByPositionScript());
        entity.addComponent(new BombScript()).explosionPrefab = scene.getAsset("Explosion", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        scene.addEntity(entity);
        return "Bomb";
    }
}
