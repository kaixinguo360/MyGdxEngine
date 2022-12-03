package com.my.world.module.particle;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.collision.BoundingBox;

public class ParticlesEffect extends BaseParticlesEffect {

    public void start() {
        getParticleEffect().start();
    }

    public void end() {
        getParticleEffect().end();
    }

    public void stop() {
        setEmissionMode(RegularEmitter.EmissionMode.Disabled);
    }

    public void reset() {
        getParticleEffect().reset();
    }

    public boolean isComplete() {
        return getParticleEffect().isComplete();
    }

    public BoundingBox getBoundingBox() {
        return getParticleEffect().getBoundingBox();
    }

    public void setEmissionMode(RegularEmitter.EmissionMode emissionMode) {
        for (ParticleController controller : getParticleEffect().getControllers()) {
            if (controller.emitter instanceof RegularEmitter) {
                RegularEmitter reg = (RegularEmitter) controller.emitter;
                reg.setEmissionMode(emissionMode);
            }
        }
    }
}
