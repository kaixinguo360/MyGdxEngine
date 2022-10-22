package com.my.demo.scene;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;
import com.my.world.module.particle.ParticlesEffect;
import com.my.world.module.particle.ParticlesSystem;

import java.util.Map;

public class ParticleScene extends BaseScene<ParticleScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        super.build(scene, params);

        String effectPath = "effect/smoke.pfx";
        ParticleEffect particleEffect = ParticlesSystem.loadParticleEffect(effectPath);

        ParticlesEffect effectWingLeft = new ParticlesEffect();
        effectWingLeft.particleEffect = particleEffect.copy();
        effectWingLeft.transform = new Matrix4().translate(-0.5f, 0, 0).rotate(Vector3.X, 90);
        aircraft.wing_L2.addComponent(effectWingLeft);

        ParticlesEffect effectWingRight = new ParticlesEffect();
        effectWingRight.particleEffect = particleEffect.copy();
        effectWingRight.transform = new Matrix4().translate(0.5f, 0, 0).rotate(Vector3.X, 90);
        aircraft.wing_R2.addComponent(effectWingRight);

        scene.addEntity(newEntity((InputSystem.OnKeyDown) keycode -> {
            if (keycode == Input.Keys.R) {
                aircraft.wing_L2.getComponents(ParticlesEffect.class).forEach(e -> e.setActive(!e.isActive()));
                aircraft.wing_R2.getComponents(ParticlesEffect.class).forEach(e -> e.setActive(!e.isActive()));
            }
        }));

        return ground;
    }

    public static Entity newEntity(Component component) {
        Entity entity = new Entity();
        entity.addComponent(component);
        return entity;
    }
}
