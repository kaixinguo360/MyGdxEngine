package com.my.game.script;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Entity;
import com.my.utils.world.com.CollisionHandler;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AircraftBombCollisionHandler extends CollisionHandler {

    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    @Override
    public void collision(Entity target) {
        if (checkVelocity(self, target, 20)) {
//                System.out.println("Boom! " + self.getId() + " ==> " + target.getId());
            physicsSystem.addExplosion(self.getComponent(Position.class).getLocalTransform().getTranslation(tmpV1), 5000);
            world.getEntityManager().getBatch().removeEntity(self.getId());
        }
    }

    private static boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }
}
