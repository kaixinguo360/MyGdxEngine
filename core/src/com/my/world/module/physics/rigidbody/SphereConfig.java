package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.RigidBodyConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SphereConfig extends RigidBodyConfig implements Loadable.OnInit {

    @Config private float radius;

    public SphereConfig(float radius, float mass) {
        this.radius = radius;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btSphereShape(radius);
        generateRigidBodyConfig();
    }
}
