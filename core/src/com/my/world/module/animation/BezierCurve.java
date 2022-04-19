package com.my.world.module.animation;

import com.badlogic.gdx.math.Vector2;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <pre>
 * Points Structure:
 *
 *
 *    ..point1..|....point2.....|.....point3.....|...point4..
 *     p0   hr0    hl1  p1  hr1    hl2  p2  hr2    hl3,  p3
 *    ....segment1.......|...segment2....|....segment3.......
 *
 *
 *    pX  ---> Point X
 *    hlX ---> Left Handle of Point X
 *    hrX ---> Right Handle of Point X
 *
 *
 * Example 1:
 *
 *     o------*O*------o
 *     |    *  |  *    |
 *     hl1*    p1   *  hr1     *
 *      *            *        *
 *     *               *     *
 *                o------*O*------o
 *                |       |       |
 *                hl2     p2      hr2
 *
 *
 * Example 2:
 *
 *          p1           hr1
 *          O------------o
 *             *
 *                *
 *                 *
 *                *
 *              *
 *           *
 *       O-------o
 *       p2      hl2
 * </pre>
 */
@NoArgsConstructor
public class BezierCurve implements Curve<Float>, Configurable {

    @Config(elementType = Vector2.class)
    private List<Vector2> points;

    public BezierCurve(List<Vector2> points) {
        this.points = points;
    }

    @Override
    public Float valueAt(float t) {
        int pointNum = (points.size() + 2) / 3;

        // Before First Point
        int firstPointIndex = 0;
        float startTime = points.get(firstPointIndex).x;
        if (t <= startTime) {
            return points.get(firstPointIndex).y;
        }

        // After Last Point
        int lastPointIndex = (pointNum - 1) * 3;
        float endTime = points.get(lastPointIndex).x;
        if (t >= endTime) {
            return points.get(lastPointIndex).y;
        }

        // Between First and Last Points
        int index = indexOf(t);
        p1.set(points.get(index));
        p2.set(points.get(index + 1));
        p3.set(points.get(index + 2));
        p4.set(points.get(index + 3));

        fcurveCorrect(p1, p2, p3, p4);
        findZero(t, p1.x, p2.x, p3.x, p4.x, opl);

        return cubicBezier(opl[0], p1.y, p2.y, p3.y, p4.y);
    }

    /**
     * Find the curve segment include the given time, return the index of left endpoint within points array
     */
    private int indexOf(float time) {
        int pointNum = (points.size() + 2) / 3;
        for (int i = 0; i < pointNum - 1; i ++) {
            int index = i * 3;
            float thisTime = points.get(index).x;
            float nextTime = points.get(index + 3).x;
            if (thisTime <= time && time <= nextTime) {
                return index;
            }
        }
        return -1;
    }

    private final Vector2 p1 = new Vector2();
    private final Vector2 p2 = new Vector2();
    private final Vector2 p3 = new Vector2();
    private final Vector2 p4 = new Vector2();
    private final float[] opl = new float[3];

    // ----- Math Utils ----- //

    private static final float SMALL = 1.0e-10f;
    private static final Vector2 tmpV1 = new Vector2();
    private static final Vector2 tmpV2 = new Vector2();

    /**
     * Cubic Bezier curve: P(t) = (1-t)^3 * p0 + 3 * (1-t)^2 * t * p1 + 3 * (1-t) * t^2 * p2 + t^3 * p3
     * @param t param t
     * @param p0 param p0
     * @param p1 param p1
     * @param p2 param p2
     * @param p3 param p3
     * @return P(t)
     */
    static float cubicBezier(float t, float p0, float p1, float p2, float p3) {
        return p0 * (1-t) * (1-t) * (1-t)
                + 3 * p1 * (1-t) * (1-t) * t
                + 3 * p2 * (1-t) * t * t
                + p3 * t * t * t;
    }

    /**
     * Find the root(s) of: x = (1-t)^3 * p0 + 3 * (1-t)^2 * t * p1 + 3 * (1-t) * t^2 * p2 + t^3 * p3
     * @param x param x
     * @param p0 param p0
     * @param p1 param p1
     * @param p2 param p2
     * @param p3 param p3
     * @param o empty array to accept roots, size should be greater than or equal to 3
     * @return number of roots
     */
    static int findZero(float x, float p0, float p1, float p2, float p3, float[] o) {
        float A, B, C, D;

        A = p3 - p0 + 3.0f * (p1 - p2);
        B = 3.0f * (p0 - 2.0f * p1 + p2);
        C = 3.0f * (p1 - p0);
        D = p0 - x;

        return solveCubic(A, B, C, D, o);
    }

    /**
     * Find the root(s) of: A * x^3 + B * x^2 + C * x^3 + D = 0
     * @param A param A
     * @param B param B
     * @param C param C
     * @param D param D
     * @param o empty array to accept roots, size should be greater than or equal to 3
     * @return number of roots
     */
    static int solveCubic(float A, float B, float C, float D, float[] o) {

        float a = B / A;
        float b = C / A;
        float c = D / A;

        float p = (float) (b - ((a * a) / 3.0));
        float q = (float) ((2 * Math.pow(a, 3) / 27.0) - (a * b / 3.0) + c);
        float delta = (float) ((Math.pow(q, 2) / 4) + (Math.pow(p, 3) / 27));

        if (delta > SMALL) {

            float mt1, mt2;
            float t1 = (float) ((-q / 2.0) + Math.sqrt(delta));
            float t2 = (float) ((-q / 2.0) - Math.sqrt(delta));

            if (t1 < 0) {
                mt1 = (float) ((-1) * (Math.pow(-t1, (float) 1 / 3)));
            } else {
                mt1 = (float) Math.pow(t1, (float) 1 / 3);
            }

            if (t2 < 0) {
                mt2 = (float) ((-1) * (Math.pow(-t2, (float) 1 / 3)));
            } else {
                mt2 = (float) Math.pow(t2, (float) 1 / 3);
            }

            o[0] = (float) (mt1 + mt2 - (a / 3.0));
            o[1] = 0;
            o[2] = 0;
            return 1;

        } else if (delta < SMALL && delta > -SMALL) {

            if (q < 0) {
                o[0] = (float) (-1 * Math.pow(-q / 2, (float) 1 / 3) - (a / 3));
                o[1] = (float) (2 * Math.pow(-q / 2, (float) 1 / 3) - (a / 3));
                o[2] = 0;
            } else {
                o[0] = (float) (Math.pow(q / 2, (float) 1 / 3) - (a / 3));
                o[1] = (float) (-2 * Math.pow(q / 2, (float) 1 / 3) - (a / 3));
                o[2] = 0;
            }
            return 2;

        } else {

            o[0] = (float) ((2.0 / Math.sqrt(3)) * (Math.sqrt(-p) * Math.sin((1 / 3.0) * Math.asin(((3 * Math.sqrt(3) * q) / (2 * Math.pow(Math.pow(-p, (float) 1 / 2), 3)))))) - (a / 3.0));
            o[1] = (float) ((-2.0 / Math.sqrt(3)) * (Math.sqrt(-p) * Math.sin((1 / 3.0) * Math.asin(((3 * Math.sqrt(3) * q) / (2 * Math.pow(Math.pow(-p, (float) 1 / 2), 3)))) + (Math.PI / 3))) - (a / 3.0));
            o[2] = (float) ((2.0 / Math.sqrt(3)) * (Math.sqrt(-p) * Math.cos((1 / 3.0) * Math.asin(((3 * Math.sqrt(3) * q) / (2 * Math.pow(Math.pow(-p, (float) 1 / 2), 3)))) + (Math.PI / 6))) - (a / 3.0));
            return 3;

        }
    }

    /**
     * Copy From Blender Source Code: fcurve_correct
     *
     * The total length of the handles is not allowed to be more
     * than the horizontal distance between (v1-v4).
     * This is to prevent curve loops.
     */

    static void fcurveCorrect(Vector2 v1, Vector2 v2, Vector2 v3, Vector2 v4) {
        Vector2 h1 = tmpV1;
        Vector2 h2 = tmpV2;
        float len1, len2, len, fac;

        /* Calculate handle deltas. */
        h1.set(v1.x - v2.x, v1.y - v2.y);
        h2.set(v4.x - v3.x, v4.y - v3.y);

        /* Calculate distances:
         * - len  = Span of time between keyframes.
         * - len1 = Length of handle of start key.
         * - len2 = Length of handle of end key.
         */
        len = v4.x - v1.x;
        len1 = Math.abs(h1.x);
        len2 = Math.abs(h2.x);

        /* If the handles have no length, no need to do any corrections. */
        if ((len1 + len2) == 0.0f) {
            return;
        }

        /* To prevent looping or rewinding, handles cannot
         * exceed the adjacent key-frames time position. */
        if (len1 > len) {
            fac = len / len1;
            v2.x = (v1.x - fac * h1.x);
            v2.y = (v1.y - fac * h1.y);
        }

        if (len2 > len) {
            fac = len / len2;
            v3.x = (v4.x - fac * h2.x);
            v3.y = (v4.y - fac * h2.y);
        }
    }
}
