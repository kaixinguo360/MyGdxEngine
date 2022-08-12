package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.script.CollisionHandler;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BombScript extends CollisionHandler {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @Config
    public EntityBuilder explosionBuilder;

    @Config
    public float triggerVelocity = 20;

    @Override
    public void collision(Entity target) {
        if (!target.contain(RigidBody.class)) return;
        if (checkVelocity(self, target)) {
//            System.out.println("Bomb! " + self.getId() + " ==> " + target.getId());
            Entity entity = explosionBuilder.build(scene);
            Matrix4 entityTransform = entity.getComponent(Position.class).getLocalTransform();
            self.getComponent(Position.class).getGlobalTransform(entityTransform);
            scene.getEntityManager().getBatch().removeEntity(self.getId());
        }
    }

    private boolean checkVelocity(Entity self, Entity target) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > triggerVelocity;
    }
}
