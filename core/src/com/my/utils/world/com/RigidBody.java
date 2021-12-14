package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.*;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class RigidBody implements Component, LoadableResource, Disposable {

    public PhysicsSystem.RigidBodyConfig bodyConfig;
    public int group;
    public int mask;
    public btRigidBody body;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        String bodyConfigId = (String) config.get("bodyConfigId");
        this.bodyConfig = assetsManager.getAsset(bodyConfigId, PhysicsSystem.RigidBodyConfig.class);
        this.body = new btRigidBody(bodyConfig.constructionInfo);
        this.group = bodyConfig.group;
        this.mask = bodyConfig.mask;
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        String bodyConfigId = assetsManager.getId(PhysicsSystem.RigidBodyConfig.class, bodyConfig);
        return new HashMap<String, Object>(){{
            put("bodyConfigId", bodyConfigId);
        }};
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
