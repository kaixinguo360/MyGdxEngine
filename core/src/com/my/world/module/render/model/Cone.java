package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Cone extends ProceduralModelRender {

    @Config private float width;
    @Config private float height;
    @Config private float depth;
    @Config private int divisions;

    public Cone(float width, float height, float depth, int divisions, Material material, long attributes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createCone(width, height, depth, divisions, material, attributes);
        super.init();
    }
}
