package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.TemplateRigidBody;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CylinderBody extends TemplateRigidBody implements Loadable.OnInit {

    @Config private Vector3 halfExtents;

    public CylinderBody(Vector3 halfExtents, float mass) {
        this.halfExtents = halfExtents;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btCylinderShape(halfExtents);
        super.init();
    }
}
