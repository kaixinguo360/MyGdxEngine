package com.my.demo.entity.weapon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.script.RemoveByTimeScript;
import com.my.world.module.animation.*;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.light.GLTFPointLight;

import java.util.ArrayList;

public class ExplosionEntity extends EnhancedEntity {

    public static TemplateRigidBody body;
    public static Playable playable;
    public static EntityBuilder builder;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        body = assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
//        body = assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));
        playable = assetsManager.addAsset("explosionAnimation", Playable.class, createExplosionAnimation());
        builder = assetsManager.addAsset("explosion", EntityBuilder.class, (EntityGenerator) ExplosionEntity::new);
    }

    public final RemoveByTimeScript removeByTimeScript;
    public final EnhancedEntity explosionScriptEntity;
    public final EnhancedEntity fireworksEffectsEntity;
    public final Animation fireworksEffectsAnimation;

    public ExplosionEntity() {
        setName("Explosion");
        removeByTimeScript = addComponent(new RemoveByTimeScript());
        removeByTimeScript.maxTime = 0.5f;

        explosionScriptEntity = new EnhancedEntity();
        explosionScriptEntity.setParent(this);
        explosionScriptEntity.setName("Explosion");
        explosionScriptEntity.addComponent(new PresetTemplateRigidBody(body, true)).isEnableCallback = true;
        explosionScriptEntity.addComponent(new ExplosionScript());
        addEntity(explosionScriptEntity);

        fireworksEffectsEntity = new EnhancedEntity();
        fireworksEffectsEntity.setParent(this);
        fireworksEffectsEntity.setName("FireworksEffects");
        fireworksEffectsEntity.addComponent(new RemoveByTimeScript()).maxTime = 0.5f;
        fireworksEffectsEntity.addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 40f, 60f));
        fireworksEffectsEntity.addComponent(new GLTFPointLight(Color.ORANGE.cpy(), 40f, 30f));
        fireworksEffectsAnimation = fireworksEffectsEntity.addComponent(new Animation());
        fireworksEffectsAnimation.useLocalTime = true;
        fireworksEffectsAnimation.addPlayable("default", playable);
        addEntity(fireworksEffectsEntity);
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
