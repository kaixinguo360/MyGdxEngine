package com.my.world.module.particle;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

public class BaseParticlesEffect extends ActivatableComponent implements ScriptSystem.OnUpdate {

    @Config(type = Config.Type.Asset)
    public ParticleEffect effect;

    @Config
    public Matrix4 transform;

    protected ParticleEffect particleEffect;
    protected Position position;

    public void registerToSystem(Scene scene, Entity entity, ParticlesSystem system) {
        position = entity.getComponent(Position.class);
        particleEffect = effect.copy();
        particleEffect.init();
        particleEffect.start();
        system.particleSystem.add(particleEffect);
    }

    public void unregisterFromSystem(Scene scene, Entity entity, ParticlesSystem system) {
        system.particleSystem.remove(particleEffect);
        particleEffect.dispose();
        particleEffect = null;
        position = null;
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (transform == null) {
            particleEffect.setTransform(position.getGlobalTransform());
        } else {
            Matrix4 tmpM = Matrix4Pool.obtain();
            particleEffect.setTransform(position.getGlobalTransform(tmpM).mul(transform));
            Matrix4Pool.free(tmpM);
        }
    }

    public boolean isInitialized() {
        return particleEffect != null;
    }

    public ParticleEffect getParticleEffect() {
        if (particleEffect == null) throw new RuntimeException("This component has not been initialized: " + this);
        return particleEffect;
    }
}
