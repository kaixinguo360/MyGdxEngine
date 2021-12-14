package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.AfterAdded;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.sys.PhysicsSystem;

public class Collisions {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    public static void initAssets(AssetsManager assetsManager) {
        assetsManager.addAsset("BombCollisionHandler", PhysicsSystem.CollisionHandler.class, new BombCollisionHandler());
        assetsManager.addAsset("BulletCollisionHandler", PhysicsSystem.CollisionHandler.class, new BulletCollisionHandler());
    }

    public static class BombCollisionHandler implements PhysicsSystem.CollisionHandler, AfterAdded {

        private World world;
        private PhysicsSystem physicsSystem;

        @Override
        public void afterAdded(World world) {
            this.world = world;
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        }

        @Override
        public void handle(Entity self, Entity target) {
            if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
                physicsSystem.addExplosion(self.getComponent(Position.class).transform.getTranslation(tmpV1), 5000);
                world.getEntityManager().getBatch().removeEntity(self.getId());
            }
        }
    }
    public static class BulletCollisionHandler implements PhysicsSystem.CollisionHandler, AfterAdded {

        private World world;
        private PhysicsSystem physicsSystem;

        @Override
        public void afterAdded(World world) {
            this.world = world;
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        }

        @Override
        public void handle(Entity self, Entity target) {
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
