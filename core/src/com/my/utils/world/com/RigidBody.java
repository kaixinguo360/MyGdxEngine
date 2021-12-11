package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.Component;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class RigidBody implements Component, Disposable {

    public PhysicsSystem.RigidBodyConfig bodyConfig;
    public int group;
    public int mask;
    public btRigidBody body;

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
