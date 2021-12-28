package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.RigidBodyConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BoxConfig extends RigidBodyConfig implements Loadable.OnInit {

    @Config private Vector3 boxHalfExtents;

    public BoxConfig(Vector3 boxHalfExtents, float mass) {
        this.boxHalfExtents = boxHalfExtents;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btBoxShape(boxHalfExtents);
        generateRigidBodyConfig();
    }
}
