package com.my.world.module.animation;

import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Array;

import static com.my.world.module.animation.GdxMatrix4Curve.getQuaternionAtTime;

public class GdxQuaternionCurve implements Curve<Quaternion> {

    protected static final Quaternion tmpQ = new Quaternion();

    protected Array<NodeKeyframe<Quaternion>> keyframes;

    public GdxQuaternionCurve(Array<NodeKeyframe<Quaternion>> keyframes) {
        if (keyframes == null) throw new RuntimeException("Keyframes is null");
        this.keyframes = keyframes;
    }

    @Override
    public Quaternion valueAt(float time) {
        return getQuaternionAtTime(keyframes, time, tmpQ);
    }
}
