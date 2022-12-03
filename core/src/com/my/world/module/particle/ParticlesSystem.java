package com.my.world.module.particle;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.my.world.core.Entity;
import com.my.world.core.EntityListener;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ParticlesSystem extends BaseParticlesSystem implements EntityListener {

    @Setter
    @Getter
    protected Texture defaultParticleTexture;

    public ParticlesSystem() {
        batches.forEach(particleSystem::add);
    }

    @Override
    protected boolean canHandle(Entity entity) {
        return entity.contain(ParticlesEffect.class) && entity.getComponent(ParticlesEffect.class).isActive();
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        List<ParticlesEffect> particlesEffects = entity.getComponents(ParticlesEffect.class);
        for (ParticlesEffect particlesEffect : particlesEffects) {
            particlesEffect.registerToSystem(scene, entity, this);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        List<ParticlesEffect> particlesEffects = entity.getComponents(ParticlesEffect.class);
        for (ParticlesEffect particlesEffect : particlesEffects) {
            particlesEffect.unregisterFromSystem(scene, entity, this);
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        billboardParticleBatch.setUseGpu(true); // Avoid a bug in BillboardParticleBatch
        super.getRenderables(renderables, pool);
    }

    @Getter protected static BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
    @Getter protected static PointSpriteParticleBatch pointSpriteParticleBatch = new PointSpriteParticleBatch();
    @Getter protected static ModelInstanceParticleBatch modelInstanceParticleBatch = new ModelInstanceParticleBatch();

    @Getter protected static Array<ParticleBatch<?>> batches = new Array<>();
    @Getter protected static ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(batches);
    @Getter protected static AssetManager assetManager = new AssetManager();

    static {
        batches.add(billboardParticleBatch);
        batches.add(pointSpriteParticleBatch);
        batches.add(modelInstanceParticleBatch);
        assetManager.setLoader(ParticleEffect.class, new ParticleEffectLoader(new InternalFileHandleResolver()));
    }

    public static ParticleEffect loadParticleEffect(String fileName) {
        assetManager.load(fileName, ParticleEffect.class, loadParam);
        assetManager.finishLoading();
        return assetManager.get(fileName, true);
    }
}
