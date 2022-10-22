package com.my.world.module.particle;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;
import lombok.Getter;

public abstract class BaseParticlesSystem extends Render implements System.OnUpdate, System.AfterAdded {

    @Getter protected ParticleSystem particleSystem;
    @Getter protected BillboardParticleBatch billboardParticleBatch;
    @Getter protected PointSpriteParticleBatch pointSpriteBatch;

    // ----- System ----- //

    public BaseParticlesSystem() {
        particleSystem = new ParticleSystem();
        billboardParticleBatch = new BillboardParticleBatch();
        pointSpriteBatch = new PointSpriteParticleBatch();
        particleSystem.add(billboardParticleBatch);
        particleSystem.add(pointSpriteBatch);
    }

    @Override
    public void update(float deltaTime) {
        particleSystem.update(deltaTime);
    }

    @Override
    public void afterAdded(Scene scene) {
        EntityFilter entityFilter = this::canHandle;
        scene.getEntityManager().addFilter(entityFilter);
        if (this instanceof EntityListener) {
            scene.getEntityManager().addListener(entityFilter, (EntityListener) this);
        }
    }
    protected abstract boolean canHandle(Entity entity);

    // ----- Render ----- //

    @Override
    public void setTransform(Position position) {}

    @Override
    public boolean isVisible(Camera cam) {
        billboardParticleBatch.setCamera(cam);
        pointSpriteBatch.setCamera(cam);
        return true;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        particleSystem.begin();
        particleSystem.draw();
        particleSystem.end();
        particleSystem.getRenderables(renderables, pool);
    }
}
