package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.world.core.Config;
import com.my.world.module.common.ActivatableComponent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Render extends ActivatableComponent {

    @Config
    public boolean includeEnv = true;

    @Config(type = Config.Type.Asset)
    public Shader shader;

    public ModelInstance modelInstance;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    public Render(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        calculateBoundingBox();
        includeEnv = true;
    }

    public void calculateBoundingBox() {
        if (this.modelInstance == null) throw new RuntimeException("ModelInstance is null");
        this.modelInstance.model.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }

    private static final BoundingBox boundingBox = new BoundingBox();
}
