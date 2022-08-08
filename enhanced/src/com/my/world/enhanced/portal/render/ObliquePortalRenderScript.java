package com.my.world.enhanced.portal.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.gdx.Matrix4Pool;

/**
 * 基于斜近平面平截头体映射矩阵实现传送门前方遮挡物体剔除
 */
public class ObliquePortalRenderScript extends PortalRenderScript {

    @Override
    protected boolean setCamera(PerspectiveCamera cam) {
        super.setCamera(cam);

//        // 基于斜近平面平截头体映射矩阵实现传送门前方遮挡物体剔除
//        Matrix4 transform = selfPosition.getGlobalTransform();
//
//        Vector3 position = Vector3.Zero.cpy().mul(transform);
//        Vector3 direction = Vector3.Z.cpy().scl(-1).rot(transform);
//        Vector4 cameraSpaceClipPlane = cameraSpacePlane(camera, position, direction);
//
////        position.set(0, 0, -5);
////        direction.set(0, 0, -1);
////        cameraSpaceClipPlane = new Vector4(direction.x, direction.y, direction.z, -direction.dot(position));
//
//        calculateObliqueMatrix(camera.projection, cameraSpaceClipPlane);
//        updateProjectionMatrix(camera);

        Vector3 position = new Vector3(0.000001f, 0.000001f, -0.000001f);
        Vector3 direction = new Vector3(0.000001f, 0.000001f, -1);
        Vector4 cameraSpaceClipPlane = new Vector4(direction.x, direction.y, direction.z, -direction.dot(position));
//        tmpM.set(cam.view).inv().tra();
//        cameraSpaceClipPlane.traMul(tmpM);
//        Vector4 clipPlaneCameraSpace = Matrix4x4.Transpose(Matrix4x4.Inverse(GetOriginalCamera().worldToCameraMatrix)) * clipPlaneWorldSpace;
        cam.update();
        calculateObliqueMatrix(cam.projection, cameraSpaceClipPlane);
        updateProjectionMatrix(cam);

        return true;
    }

    public void updateProjectionMatrix(PerspectiveCamera camera) {
        camera.combined.set(camera.projection).mul(camera.view);
//        camera.invProjectionView.set(camera.combined).inv();
//        camera.frustum.update(camera.invProjectionView);
    }

    public static Vector4 cameraSpacePlane(Camera camera, Vector3 position, Vector3 direction) {
        Matrix4 view = Matrix4Pool.obtain().set(camera.view);
        position.mul(view);
        direction.nor().rot(view);
        Matrix4Pool.free(view);

        if (direction.dot(Vector3.Z) > 0) {
            direction.scl(-1);
        }

        return new Vector4(direction.x, direction.y, direction.z, -direction.dot(position));
    }

    public static void calculateObliqueMatrix(Matrix4 matrix, Vector4 clipPlane) {
//        Vector4 q = new Vector4(
//                sgn(clipPlane.x),
//                sgn(clipPlane.y),
//                1.0f,
//                1.0f
//        ).traMul(matrix.cpy().inv());
//        Vector4 c = clipPlane.scl(2.0F / (clipPlane.dot(q)));
//        // third row = clip plane - fourth row
//        matrix.val[2] = c.x - matrix.val[3];
//        matrix.val[6] = c.y - matrix.val[7];
//        matrix.val[10] = c.z - matrix.val[11];
//        matrix.val[14] = c.w - matrix.val[15];

        // Calculate the clip-space corner point opposite the clipping plane
        // as (sgn(clipPlane.x), sgn(clipPlane.y), 1, 1) and
        // transform it into camera space by multiplying it
        // by the inverse of the projection matrix
        Vector4 q = new Vector4(
                (sgn(clipPlane.x) + matrix.val[8]) / matrix.val[0],
                (sgn(clipPlane.y) + matrix.val[9]) / matrix.val[5],
                -1.0F,
                (1.0F + matrix.val[10]) / matrix.val[14]
        );

        // Calculate the scaled plane vector
//        Vector4 c = clipPlane * (2.0F / Dot(clipPlane, q));
        Vector4 c = clipPlane.scl(2.0F / clipPlane.dot(q));

        // Replace the third row of the projection matrix
        matrix.val[2] = c.x;
        matrix.val[6] = c.y;
        matrix.val[10] = c.z + 1.0F;
        matrix.val[14] = c.w;

    }

    public static float sgn(float a) {
        if (a > 0.0f) return 1.0f;
        if (a < 0.0f) return -1.0f;
        return 0.0f;
    }

    public static class Vector4 {

        public float x;
        public float y;
        public float z;
        public float w;

        public Vector4(float x, float y, float z, float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public Vector4 mul(Matrix4 matrix) {
            final float[] l_mat = matrix.val;
            x = x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + w * l_mat[Matrix4.M03];
            y = x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + w * l_mat[Matrix4.M13];
            z = x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + w * l_mat[Matrix4.M23];
            w = x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + w * l_mat[Matrix4.M33];
            return this;
        }

        public Vector4 traMul(Matrix4 inv) {
            final float[] l_mat = inv.val;
            x = x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M10] + z * l_mat[Matrix4.M20] + w * l_mat[Matrix4.M30];
            y = x * l_mat[Matrix4.M01] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M21] + w * l_mat[Matrix4.M31];
            z = x * l_mat[Matrix4.M02] + y * l_mat[Matrix4.M12] + z * l_mat[Matrix4.M22] + w * l_mat[Matrix4.M32];
            w = x * l_mat[Matrix4.M03] + y * l_mat[Matrix4.M13] + z * l_mat[Matrix4.M23] + w * l_mat[Matrix4.M33];
            return this;
        }

        public float dot(Vector4 vector) {
            return x * vector.x + y * vector.y + z * vector.z + w * vector.w;
        }

        public Vector4 scl(float f) {
            x = x * f;
            y = x * f;
            x = x * f;
            w = x * f;
            return this;
        }
    }
}
