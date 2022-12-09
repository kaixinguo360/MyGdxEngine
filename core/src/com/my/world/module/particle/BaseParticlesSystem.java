package com.my.world.module.particle;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.render.BaseRender;
import com.my.world.module.render.RenderSystem;
import lombok.Getter;

public abstract class BaseParticlesSystem extends BaseRender implements System.OnStart, System.OnUpdate, System.AfterAdded {

    @Getter
    protected final ParticleSystem particleSystem = new ParticleSystem();
    protected Scene scene;

    // ----- System ----- //

    @Override
    public void afterAdded(Scene scene) {
        this.scene = scene;
        EntityFilter entityFilter = this::canHandle;
        scene.getEntityManager().addFilter(entityFilter);
        if (this instanceof EntityListener) {
            scene.getEntityManager().addListener(entityFilter, (EntityListener) this);
        }
    }
    protected abstract boolean canHandle(Entity entity);

    @Override
    public void start(Scene scene) {
        RenderSystem renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
        if (renderSystem == null) throw new RuntimeException("Required System not found: RenderSystem");
        renderSystem.extraRenders.add(this);
    }

    @Override
    public void update(float deltaTime) {
        particleSystem.update(deltaTime);
    }

    // ----- Render ----- //

    @Override
    public void setTransform(Position position) {}

    @Override
    public boolean isVisible(Camera cam) {
        for (ParticleBatch<?> batch : particleSystem.getBatches()) {
            if (batch instanceof BillboardParticleBatch) {
                ((BillboardParticleBatch) batch).setCamera(cam);
            }
            if (batch instanceof PointSpriteParticleBatch) {
                ((PointSpriteParticleBatch) batch).setCamera(cam);
            }
        }
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
