package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Capsule extends ProceduralModelRender {

    @Config private float radius;
    @Config private float height;
    @Config private int divisions;

    public Capsule(float radius, float height, int divisions, Material material, long attributes) {
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createCapsule(radius, height, divisions, material, attributes);
        super.init();
    }
}
