package com.my.world.module.particle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteRenderer;
import com.my.world.core.Entity;
import com.my.world.core.EntityListener;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ParticlesSystem extends BaseParticlesSystem implements EntityListener {

    @Setter
    @Getter
    protected Texture defaultParticleTexture;

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
            Texture texture = particlesEffect.particleTexture;
            if (texture == null) {
                texture = defaultParticleTexture;
            }
            for (ParticleController controller : particlesEffect.particleEffect.getControllers()) {
                addController(texture, controller);
            }
        }
    }

    private void addController(Texture texture, ParticleController controller) {
        boolean foundCompatible = false;
        for (ParticleBatch<?> batch : particleSystem.getBatches()) {
            if (controller.renderer.isCompatible(batch)) {
                if (batch instanceof BillboardParticleBatch && ((BillboardParticleBatch) batch).getTexture() != texture) {
                    continue;
                }
                if (batch instanceof PointSpriteParticleBatch && ((PointSpriteParticleBatch) batch).getTexture() != texture) {
                    continue;
                }
                controller.renderer.setBatch(batch);
                foundCompatible = true;
                break;
            }
        }
        if (!foundCompatible) {
            ParticleBatch<?> batch = null;
            if (controller.renderer instanceof BillboardRenderer) {
                batch = new BillboardParticleBatch();
                ((BillboardParticleBatch) batch).setUseGpu(true);
                ((BillboardParticleBatch) batch).setAlignMode(ParticleShader.AlignMode.Screen);
                ((BillboardParticleBatch) batch).setTexture(texture);
            }
            if (controller.renderer instanceof PointSpriteRenderer) {
                batch = new PointSpriteParticleBatch();
                ((PointSpriteParticleBatch) batch).setTexture(texture);
            }
            if (controller.renderer instanceof ModelInstanceRenderer) {
                batch = new ModelInstanceParticleBatch();
            }
            if (controller.renderer instanceof ParticleControllerControllerRenderer) {
                ParallelArray.ObjectChannel<ParticleController> channel = controller.particles.getChannel(ParticleChannels.ParticleController);
                for (int i = 0, c = channel.data.length; i < c; ++i) {
                    addController(texture, channel.data[i]);
                }
            }
            if (batch != null && controller.renderer.setBatch(batch)) {
                particleSystem.add(batch);
            }
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
