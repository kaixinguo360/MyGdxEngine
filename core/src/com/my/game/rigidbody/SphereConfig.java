package com.my.game.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.my.utils.world.Config;
import com.my.utils.world.Loadable;
import com.my.utils.world.com.RigidBodyConfig;
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
