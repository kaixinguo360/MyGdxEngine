package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.my.world.core.Config;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.Position;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BaseRender extends Render {

    @Config
    public boolean isAlwaysVisible = false;

    public ModelInstance modelInstance;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    public BaseRender(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        calculateBoundingBox();
        includeEnv = true;
    }

    @Override
    public void setTransform(Position position) {
        position.getGlobalTransform(this.modelInstance.transform);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        modelInstance.getRenderables(renderables, pool);
    }

    @Override
    public boolean isVisible(PerspectiveCamera cam) {
        if (isAlwaysVisible) return true;
        Vector3 tmpV = Vector3Pool.obtain();
        modelInstance.transform.getTranslation(tmpV);
        tmpV.add(this.center);
        boolean b = cam.frustum.sphereInFrustum(tmpV, this.radius);
        Vector3Pool.free(tmpV);
        return b;
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
