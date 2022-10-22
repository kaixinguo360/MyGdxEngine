package com.my.world.module.particle;

import com.my.world.core.Entity;
import com.my.world.core.EntityListener;

import java.util.List;

public class ParticlesSystem extends BaseParticlesSystem implements EntityListener {

    @Override
    protected boolean canHandle(Entity entity) {
        return entity.contain(ParticlesEffect.class) && entity.getComponent(ParticlesEffect.class).isActive();
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        List<ParticlesEffect> particlesEffects = entity.getComponents(ParticlesEffect.class);
        for (ParticlesEffect particlesEffect : particlesEffects) {
            particlesEffect.particleEffect.init();
            particlesEffect.particleEffect.start();
            particleSystem.add(particlesEffect.particleEffect);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        List<ParticlesEffect> particlesEffects = entity.getComponents(ParticlesEffect.class);
        for (ParticlesEffect particlesEffect : particlesEffects) {
            particleSystem.remove(particlesEffect.particleEffect);
        }
    }
}
