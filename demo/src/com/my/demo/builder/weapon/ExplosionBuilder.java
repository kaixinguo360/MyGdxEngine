package com.my.demo.builder.weapon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.enhanced.script.RemoveByTimeScript;
import com.my.world.module.animation.*;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.light.GLTFPointLight;

import java.util.ArrayList;

public class ExplosionBuilder extends PrefabBuilder<ExplosionBuilder> {

    {
        prefabName = "Explosion";
    }

    public TemplateRigidBody body;
    public Playable playable;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        body = assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
//        body = assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));
        playable = assetsManager.addAsset("explosionAnimation", Playable.class, createExplosionAnimation());
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new RemoveByTimeScript()).maxTime = 0.5f;
        scene.addEntity(entity);

        Entity explosionScriptEntity = new Entity();
        explosionScriptEntity.setParent(entity);
        explosionScriptEntity.setName("Explosion");
        explosionScriptEntity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = body;
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
        animation.addPlayable("default", playable);
        scene.addEntity(fireworksEffectsEntity);
    }

    // ----- Static ----- //

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
}
