package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RigidBody implements Component, Loadable.OnInit, Disposable {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config(type = Config.Type.Asset)
    public btRigidBody.btRigidBodyConstructionInfo bodyConfig;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    public btRigidBody body;

    public RigidBody(btRigidBody.btRigidBodyConstructionInfo bodyConfig) {
        this.bodyConfig = bodyConfig;
        init();
    }

    @Override
    public void init() {
        this.body = new btRigidBody(bodyConfig);
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
