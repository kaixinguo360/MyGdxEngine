package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Sphere extends ProceduralModelRender {

    @Config private float width;
    @Config private float height;
    @Config private float depth;
    @Config private int divisionsU;
    @Config private int divisionsV;

    public Sphere(float width, float height, float depth, int divisionsU, int divisionsV, Material material, long attributes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.divisionsU = divisionsU;
        this.divisionsV = divisionsV;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createSphere(width, height, depth, divisionsU, divisionsV, material, attributes);
        super.init();
    }
}
