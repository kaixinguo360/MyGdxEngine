package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.RigidBodyConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConeConfig extends RigidBodyConfig implements Loadable.OnInit {

    @Config private float radius;
    @Config private float height;

    public ConeConfig(float radius, float height, float mass) {
        this.radius = radius;
        this.height = height;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btConeShape(radius, height);
        generateRigidBodyConfig();
    }
}
