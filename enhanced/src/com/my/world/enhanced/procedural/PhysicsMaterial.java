package com.my.world.enhanced.procedural;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Configurable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

@NoArgsConstructor
@AllArgsConstructor
public class PhysicsMaterial implements Configurable {

    public Material material;
    public long attributes;
    public float density;
    public Consumer<btRigidBody> config;

    public float getMass(float volume) {
        return density * volume;
    }

    public void config(btRigidBody body) {
        if (config != null) config.accept(body);
    }

}
