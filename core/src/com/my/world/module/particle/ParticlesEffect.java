package com.my.world.module.particle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

public class ParticlesEffect extends ActivatableComponent implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    @Config
    public Texture particleTexture;

    @Config
    public ParticleEffect particleEffect;

    @Config
    public Matrix4 transform;

    protected Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        position = entity.getComponent(Position.class);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (transform == null) {
            particleEffect.setTransform(position.getGlobalTransform());
        } else {
            Matrix4 tmpM = Matrix4Pool.obtain();
            particleEffect.setTransform(position.getGlobalTransform(tmpM).mul(transform));
            Matrix4Pool.free(tmpM);
        }
    }
}
