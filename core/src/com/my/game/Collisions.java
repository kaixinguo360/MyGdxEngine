package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.sys.PhysicsSystem;

public class Collisions {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    public static void init(World world) {
        AssetsManager assetsManager = world.getAssetsManager();
        PhysicsSystem physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        assetsManager.addAsset("BombCollisionHandler", PhysicsSystem.CollisionHandler.class, (self, target) -> {
            if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
                physicsSystem.addExplosion(self.getComponent(Position.class).getTransform().getTranslation(tmpV1), 5000);
                world.getEntityManager().getBatch().removeEntity(self.getId());
            }
        });
        assetsManager.addAsset("BulletCollisionHandler", PhysicsSystem.CollisionHandler.class, (self, target) -> {
            if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
                physicsSystem.addExplosion(self.getComponent(Position.class).getTransform().getTranslation(tmpV1), 5000);
                world.getEntityManager().getBatch().removeEntity(self.getId());
            }
        });
    }

    private static boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }

}
