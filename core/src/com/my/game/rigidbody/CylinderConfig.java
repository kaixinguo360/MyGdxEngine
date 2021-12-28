package com.my.game.rigidbody;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.my.utils.world.Config;
import com.my.utils.world.Loadable;
import com.my.utils.world.com.RigidBodyConfig;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CylinderConfig extends RigidBodyConfig implements Loadable.OnInit {

    @Config private Vector3 halfExtents;

    public CylinderConfig(Vector3 halfExtents, float mass) {
        this.halfExtents = halfExtents;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btCylinderShape(halfExtents);
        generateRigidBodyConfig();
    }
}
