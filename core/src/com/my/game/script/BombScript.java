package com.my.game.script;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import com.my.utils.world.Prefab;
import com.my.utils.world.com.CollisionHandler;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BombScript extends CollisionHandler {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @Config
    public Prefab explosionPrefab;

    @Config
    public float triggerVelocity = 20;

    @Override
    public void collision(Entity target) {
        if (!target.contain(RigidBody.class)) return;
        if (checkVelocity(self, target)) {
            System.out.println("Bomb! " + self.getId() + " ==> " + target.getId());
            Matrix4 selfTransform = self.getComponent(Position.class).getLocalTransform();
            Entity entity = explosionPrefab.newInstance(scene);
            entity.getComponent(Position.class).setLocalTransform(selfTransform);
            scene.getEntityManager().getBatch().removeEntity(self.getId());
        }
    }

    private boolean checkVelocity(Entity self, Entity target) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > triggerVelocity;
    }
}
