package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CapsuleModel extends BaseModel {

    @Config private float radius;
    @Config private float height;
    @Config private int divisions;
    @Config private Color color;
    @Config private long attributes;

    public CapsuleModel(float radius, float height, int divisions, Color color, long attributes) {
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.color = color;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createCapsule(radius, height, divisions, new Material(ColorAttribute.createDiffuse(color)), attributes);
        calculateBoundingBox();
    }
}
