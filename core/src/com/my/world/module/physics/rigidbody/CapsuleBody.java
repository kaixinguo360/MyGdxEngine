package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.physics.TemplateRigidBody;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CapsuleBody extends TemplateRigidBody implements Configurable.OnInit {

    @Config private float radius;
    @Config private float height;

    public CapsuleBody(float radius, float height, float mass) {
        this(radius, height, mass, false);
    }

    public CapsuleBody(float radius, float height, float mass, boolean isTrigger) {
        super(isTrigger);
        this.radius = radius;
        this.height = height;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        shape = new btCapsuleShape(radius, height);
        super.init();
    }
}
