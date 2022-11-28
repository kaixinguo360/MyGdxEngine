package com.my.demo.entity.weapon;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.particle.ParticlesEffect;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.script.CollisionHandler;
import com.my.world.module.render.Render;
import com.my.world.module.script.ScriptSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BombScript extends CollisionHandler implements ScriptSystem.OnUpdate {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @Config
    public EntityBuilder explosionBuilder;

    @Config
    public float waitTimeBeforeRemove = 10;

    @Config
    public float triggerVelocity = 20;

    protected boolean isExploited = false;
    protected float timeToLive;

    @Override
    public void collision(Entity target) {
        if (!target.contain(RigidBody.class)) return;
        if (checkVelocity(self, target)) {
            explosion();
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (!isExploited) return;
        timeToLive -= scene.getTimeManager().getDeltaTime();
        if (timeToLive > 0) return;
        scene.getEntityManager().getBatch().removeEntity(self.getId());
    }

    public void explosion() {
//        System.out.println("Bomb! " + self.getId() + " ==> " + target.getId());
        if (isExploited) return;
        isExploited = true;
        Entity entity = explosionBuilder.build(scene);
        Matrix4 entityTransform = entity.getComponent(Position.class).getLocalTransform();
        self.getComponent(Position.class).getGlobalTransform(entityTransform);
        if (waitTimeBeforeRemove == 0) {
            scene.getEntityManager().getBatch().removeEntity(self.getId());
        } else {
            timeToLive = waitTimeBeforeRemove;
            Render render = self.getComponent(Render.class);
            if (render != null) render.setActive(false);
            RigidBody rigidBody = self.getComponent(RigidBody.class);
            if (rigidBody != null) rigidBody.setActive(false);
            ParticlesEffect particlesEffect = self.getComponent(ParticlesEffect.class);
            if (particlesEffect != null) {
                for (ParticleController controller : particlesEffect.particleEffect.getControllers()) {
                    if (controller.emitter instanceof RegularEmitter) {
                        RegularEmitter reg = (RegularEmitter) controller.emitter;
                        reg.setEmissionMode(RegularEmitter.EmissionMode.Disabled);
                    }
                }
            }
        }
    }

    protected boolean checkVelocity(Entity self, Entity target) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > triggerVelocity;
    }
}
