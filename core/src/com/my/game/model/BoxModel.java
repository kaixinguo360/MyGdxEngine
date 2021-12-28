package com.my.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.my.utils.world.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BoxModel extends BaseModel {

    @Config private float width;
    @Config private float height;
    @Config private float depth;
    @Config private Color color;
    @Config private long attributes;

    public BoxModel(float width, float height, float depth, Color color, long attributes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.color = color;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        model = mdBuilder.createBox(width, height, depth, new Material(ColorAttribute.createDiffuse(color)), attributes);
        calculateBoundingBox();
    }
}
