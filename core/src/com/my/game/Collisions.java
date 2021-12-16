package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import lombok.NoArgsConstructor;

public class Collisions {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @NoArgsConstructor
    public static class BombCollisionHandler extends Collision {

        public BombCollisionHandler(int callbackFlag, int callbackFilter) {
            super(callbackFlag, callbackFilter);
        }

        @Override
        public void handle(Entity target) {
            if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
                physicsSystem.addExplosion(self.getComponent(Position.class).transform.getTranslation(tmpV1), 5000);
                world.getEntityManager().getBatch().removeEntity(self.getId());
            }
        }
    }

    @NoArgsConstructor
    public static class BulletCollisionHandler extends Collision {

        public BulletCollisionHandler(int callbackFlag, int callbackFilter) {
            super(callbackFlag, callbackFilter);
        }

        @Override
        public void handle(Entity target) {
            if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
                physicsSystem.addExplosion(self.getComponent(Position.class).transform.getTranslation(tmpV1), 5000);
                world.getEntityManager().getBatch().removeEntity(self.getId());
            }
        }
    }

    private static boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }

}
