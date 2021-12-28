package com.my.game.rigidbody;

import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.my.utils.world.Config;
import com.my.utils.world.Loadable;
import com.my.utils.world.com.RigidBodyConfig;
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
