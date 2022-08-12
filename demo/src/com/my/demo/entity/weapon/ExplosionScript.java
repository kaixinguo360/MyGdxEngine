package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;

public class ExplosionScript implements ScriptSystem.OnStart, PhysicsSystem.OnCollision {

    @Config
    public float maxForce = 10000;

    private Scene scene;
    private Entity self;

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
        this.self = entity;
    }

    @Override
    public void collision(Entity entity) {

        if (entity.contain(RigidBody.class)) {
            Vector3 tmpV1 = Vector3Pool.obtain();
            Vector3 tmpV2 = Vector3Pool.obtain();

            self.getComponent(Position.class).getGlobalTransform().getTranslation(tmpV1);
            entity.getComponent(Position.class).getGlobalTransform().getTranslation(tmpV2);

            tmpV2.sub(tmpV1);
            float len2 = tmpV2.len2();
            tmpV2.nor().scl(maxForce * 1/len2);
//            System.out.println("Explosion:\t" + entity.getName() + "\tforce:\t" + tmpV2.len());
            RigidBody rigidBody = entity.getComponent(RigidBody.class);
            rigidBody.body.activate();
            rigidBody.body.applyCentralImpulse(tmpV2);

            Vector3Pool.free(tmpV1);
            Vector3Pool.free(tmpV2);
        }

        scene.getEntityManager().getBatch().removeEntity(self.getId());
    }
}
