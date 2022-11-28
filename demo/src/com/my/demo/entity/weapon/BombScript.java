package com.my.demo.entity.weapon;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BombScript extends CollisionHandler implements ScriptSystem.OnUpdate {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @Config
    public EntityBuilder explosionBuilder;

    @Config
    public float triggerVelocity = 20;

    @Config
    public float waitTimeBeforeRemove = 10;

    @Config
    @Getter
    protected boolean isExploited = false;

    @Override
    public void collision(Entity target) {
        if (checkVelocity(self, target)) {
            explosion();
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (!isExploited) return;
        waitTimeBeforeRemove -= scene.getTimeManager().getDeltaTime();
        if (waitTimeBeforeRemove > 0) return;
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
            Render render = self.getComponent(Render.class);
            if (render != null) render.setActive(false);
            RigidBody rigidBody = self.getComponent(RigidBody.class);
            if (rigidBody != null) rigidBody.setActive(false);
            ParticlesEffect particlesEffect = self.getComponent(ParticlesEffect.class);
            if (particlesEffect != null) particlesEffect.stop();
        }
    }

    protected boolean checkVelocity(Entity self, Entity target) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        if (target.contain(RigidBody.class)) {
            tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
            tmpV1.sub(tmpV2);
        }
        return tmpV1.len() > triggerVelocity;
    }
}
