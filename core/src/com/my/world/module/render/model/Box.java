package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Box extends ProceduralModelRender {

    @Config private float width;
    @Config private float height;
    @Config private float depth;

    public Box(float width, float height, float depth, Material material, long attributes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createBox(width, height, depth, material, attributes);
        super.init();
    }
}
