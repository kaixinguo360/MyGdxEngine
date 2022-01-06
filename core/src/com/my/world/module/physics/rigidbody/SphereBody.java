package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.TemplateRigidBody;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SphereBody extends TemplateRigidBody implements Loadable.OnInit {

    @Config private float radius;

    public SphereBody(float radius, float mass) {
        this(radius, mass, false);
    }

    public SphereBody(float radius, float mass, boolean isTrigger) {
        super(isTrigger);
        this.radius = radius;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btSphereShape(radius);
        super.init();
    }
}
