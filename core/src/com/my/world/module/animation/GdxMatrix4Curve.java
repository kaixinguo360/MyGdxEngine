package com.my.world.module.animation;

import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GdxMatrix4Curve implements Curve<Matrix4> {

    protected static final BaseAnimationController.Transform tmpT = new BaseAnimationController.Transform();
    protected static final Matrix4 tmpM = new Matrix4();

    protected NodeAnimation nodeAnim;

    public GdxMatrix4Curve(NodeAnimation nodeAnim) {
        if (nodeAnim == null) throw new RuntimeException("NodeAnim is null");
        this.nodeAnim = nodeAnim;
    }

    @Override
    public Matrix4 valueAt(float time) {
        BaseAnimationController.Transform transform = getNodeAnimationTransform(nodeAnim, time);
        transform.toMatrix4(tmpM);
        return tmpM;
    }

    // ----- Static ----- //

    /**
     * Find first key frame index just before a given time
     *
     * @param keyframes  Key frames ordered by time ascending
     * @param time Time to search
     * @return key frame index, 0 if time is out of key frames time range
     */
    public static <T> int getFirstKeyframeIndexAtTime(final Array<NodeKeyframe<T>> keyframes, final float time) {
        final int lastIndex = keyframes.size - 1;

        // edges cases 1 : time out of range always return first index
        if (lastIndex <= 0 || time < keyframes.get(0).keytime) {
            return 0;
        }
        // edges cases 2 : time out of range always return last index
        if (time > keyframes.get(lastIndex).keytime) {
            return lastIndex;
        }

        // binary search
        int minIndex = 0;
        int maxIndex = lastIndex;

        while (minIndex < maxIndex) {
            int i = (minIndex + maxIndex) / 2;
            if (time > keyframes.get(i + 1).keytime) {
                minIndex = i + 1;
            } else if (time < keyframes.get(i).keytime) {
                maxIndex = i - 1;
            } else {
                return i;
            }
        }
        return minIndex;
    }

    public static Vector3 getVector3AtTime(final Array<NodeKeyframe<Vector3>> keyframes, final float time, final Vector3 out) {
        if (keyframes.size == 1) {
            out.set(keyframes.get(0).value);
            return out;
        }

        int index = getFirstKeyframeIndexAtTime(keyframes, time);

        if (index < keyframes.size - 1) {
            final NodeKeyframe<Vector3> firstKeyframe = keyframes.get(index);
            final NodeKeyframe<Vector3> secondKeyframe = keyframes.get(index + 1);
            final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
            out.set(firstKeyframe.value);
            out.lerp(secondKeyframe.value, t);
        } else {
            final NodeKeyframe<Vector3> lastKeyframe = keyframes.peek();
            out.set(lastKeyframe.value);
        }

        return out;
    }

    public static Quaternion getQuaternionAtTime(final Array<NodeKeyframe<Quaternion>> keyframes, final float time, final Quaternion out) {
        if (keyframes.size == 1) {
            out.set(keyframes.get(0).value);
            return out;
        }

        int index = getFirstKeyframeIndexAtTime(keyframes, time);

        if (index < keyframes.size - 1) {
            final NodeKeyframe<Quaternion> firstKeyframe = keyframes.get(index);
            final NodeKeyframe<Quaternion> secondKeyframe = keyframes.get(index + 1);
            final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
            out.set(firstKeyframe.value);
            out.slerp(secondKeyframe.value, t);
        } else {
            final NodeKeyframe<Quaternion> lastKeyframe = keyframes.peek();
            out.set(lastKeyframe.value);
        }

        return out;
    }

    public static BaseAnimationController.Transform getNodeAnimationTransform(final NodeAnimation nodeAnim, final float time) {

        if (nodeAnim.translation == null) tmpT.translation.set(nodeAnim.node.translation);
        else getVector3AtTime(nodeAnim.translation, time, tmpT.translation);

        if (nodeAnim.rotation == null) tmpT.rotation.set(nodeAnim.node.rotation);
        else getQuaternionAtTime(nodeAnim.rotation, time, tmpT.rotation);

        if (nodeAnim.scaling == null) tmpT.scale.set(nodeAnim.node.scale);
        else getVector3AtTime(nodeAnim.scaling, time, tmpT.scale);

        return tmpT;
    }
}
