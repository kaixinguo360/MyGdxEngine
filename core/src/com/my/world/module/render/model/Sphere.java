package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.my.world.core.Config;
import com.my.world.module.render.ModelRender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Sphere extends ModelRender {

    @Config private float width;
    @Config private float height;
    @Config private float depth;
    @Config private int divisionsU;
    @Config private int divisionsV;
    @Config private Color color;
    @Config private long attributes;

    public Sphere(float width, float height, float depth, int divisionsU, int divisionsV, Color color, long attributes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.divisionsU = divisionsU;
        this.divisionsV = divisionsV;
        this.color = color;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createSphere(width, height, depth, divisionsU, divisionsV, new Material(ColorAttribute.createDiffuse(color)), attributes);
        super.init();
    }
}
