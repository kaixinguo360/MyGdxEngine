package com.my.demo.entity.weapon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.script.RemoveByTimeScript;
import com.my.world.module.animation.*;
import com.my.world.module.particle.ParticlesEffect;
import com.my.world.module.particle.ParticlesSystem;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.light.GLTFPointLight;

import java.util.ArrayList;

public class ExplosionEntity extends EnhancedEntity {

    public static String effectPath = "effect/explosion.pfx";

    public static TemplateRigidBody body;
    public static Playable playable;
    public static ParticleEffect particleEffect;
    public static EntityBuilder builder;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        body = assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
//        body = assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));
        playable = assetsManager.addAsset("explosionAnimation", Playable.class, createExplosionAnimation());
        particleEffect = assetsManager.addAsset("explosionEffect", ParticleEffect.class, ParticlesSystem.loadParticleEffect(effectPath));
        builder = assetsManager.addAsset("explosion", EntityBuilder.class, (EntityGenerator) ExplosionEntity::new);
    }

    public final RemoveByTimeScript removeByTimeScript;
    public final EnhancedEntity explosionScriptEntity;
    public final EnhancedEntity fireworksEffectsEntity;
    public final Animation fireworksEffectsAnimation;

    public ExplosionEntity() {
        setName("Explosion");
        removeByTimeScript = addComponent(new RemoveByTimeScript());
        removeByTimeScript.maxTime = 1;

        explosionScriptEntity = new EnhancedEntity();
        explosionScriptEntity.setParent(this);
        explosionScriptEntity.setName("Explosion");
        explosionScriptEntity.addComponent(new PresetTemplateRigidBody(body, true)).isEnableCallback = true;
        explosionScriptEntity.addComponent(new ExplosionScript());
        addEntity(explosionScriptEntity);

        fireworksEffectsEntity = new EnhancedEntity();
        fireworksEffectsEntity.setParent(this);
        fireworksEffectsEntity.setName("FireworksEffects");
        fireworksEffectsEntity.addComponent(new RemoveByTimeScript()).maxTime = 1f;
        fireworksEffectsEntity.addComponent(new GLTFPointLight(Color.ORANGE.cpy(), 40f, 60f));
        fireworksEffectsAnimation = fireworksEffectsEntity.addComponent(new Animation());
        fireworksEffectsAnimation.addPlayable("default", playable);
        fireworksEffectsEntity.addComponent(new ParticlesEffect()).particleEffect = particleEffect.copy();
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
            add(new Vector2(0, 400));
            add(new Vector2(0.1f, 400));

            add(new Vector2(0.1f, 800));
            add(new Vector2(0.2f, 800));
            add(new Vector2(0.25f, 800));

            add(new Vector2(0.55f, 1600));
            add(new Vector2(0.6f, 1600));
            add(new Vector2(0.65f, 1600));

            add(new Vector2(0.3f, 0));
            add(new Vector2(0.7f, 0));
        }});
        clip.channels.add(c1);

        // Channel - light[0].range
        AnimationChannel c2 = new AnimationChannel();
        c2.component = GLTFPointLight.class;
        c2.index = 0;
        c2.field = "light.range";
        c2.values = new BezierCurve(new ArrayList<Vector2>(){{
            add(new Vector2(0, 30f));
            add(new Vector2(0.1f, 30f));

            add(new Vector2(0.1f, 100f));
            add(new Vector2(0.2f, 100f));
            add(new Vector2(0.3f, 100f));

            add(new Vector2(0.55f, 160));
            add(new Vector2(0.6f, 160));
            add(new Vector2(0.65f, 160));

            add(new Vector2(0.4f, 0f));
            add(new Vector2(1f, 0f));
        }});
        clip.channels.add(c2);

        return clip;
    }
}
