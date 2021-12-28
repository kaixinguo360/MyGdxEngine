package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RenderModel {

    private static final BoundingBox boundingBox = new BoundingBox();

    public Model model;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    public RenderModel(Model model) {
        this.model = model;
        calculateBoundingBox();
    }

    public void calculateBoundingBox() {
        model.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }
}
