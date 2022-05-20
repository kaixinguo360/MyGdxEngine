package com.my.world.module.animation;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import lombok.val;
import org.junit.Test;

public class AnimationChannelTest {

    @Test
    public void testBlendTwoValues() {
        blendTwoValues(new Vector3(1, 1, 1), new Vector3(2, 2, 2));
        blendTwoValues(new Quaternion(1, 1, 1, 1), new Quaternion(2, 2, 2, 2));
    }

    public static void blendTwoValues(Object v1, Object v2) {
        System.out.print(v1 + " + " + v2 + " = ");
        val result = AnimationChannel.blendTwoValues(v1, v2);
        System.out.println(result);
    }

    @Test
    public void testApplyWeightsToValue() {
        applyWeightsToValue(2, new Vector3(1, 2, 3));
        applyWeightsToValue(2, new Quaternion(1, 1, 1, 1));
    }

    public static void applyWeightsToValue(float weights, Object value) {
        System.out.print(weights + " * " + value + " = ");
        val result = AnimationChannel.applyWeightsToValue(weights, value);
        System.out.println(result);
    }
}
