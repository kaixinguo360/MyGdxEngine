package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.RigidBodyConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CapsuleConfig extends RigidBodyConfig implements Loadable.OnInit {

    @Config private float radius;
    @Config private float height;

    public CapsuleConfig(float radius, float height, float mass) {
        this.radius = radius;
        this.height = height;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btCapsuleShape(radius, height);
        generateRigidBodyConfig();
    }
}
