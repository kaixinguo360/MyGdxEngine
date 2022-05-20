package com.my.world.module.animation;

import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import static com.my.world.module.animation.GdxMatrix4Curve.getVector3AtTime;

public class GdxVector3Curve implements Curve<Vector3> {

    protected static final Vector3 tmpV = new Vector3();

    protected Array<NodeKeyframe<Vector3>> keyframes;

    public GdxVector3Curve(Array<NodeKeyframe<Vector3>> keyframes) {
        if (keyframes == null) throw new RuntimeException("Keyframes is null");
        this.keyframes = keyframes;
    }

    @Override
    public Vector3 valueAt(float time) {
        return getVector3AtTime(keyframes, time, tmpV);
    }
}
