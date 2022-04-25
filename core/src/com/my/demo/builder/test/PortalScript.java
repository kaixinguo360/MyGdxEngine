package com.my.demo.builder.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.GL30.GL_DEPTH24_STENCIL8;
import static com.badlogic.gdx.graphics.GL30.GL_DEPTH_STENCIL_ATTACHMENT;

public class PortalScript implements ScriptSystem.OnStart, CameraSystem.BeforeRender, CameraSystem.AfterRender {

    @Config
    public final Matrix4 targetTransform = new Matrix4();

    @Config
    public float radius = 1;

    private Position position;
    private Render render;
    private RenderSystem renderSystem;

    private Shader shader;
    private final ModelBatch batch = new ModelBatch();
    private final PerspectiveCamera camera = new PerspectiveCamera();

    private static final int frameBuffer;

    @Override
    public void start(Scene scene, Entity entity) {
        this.position = entity.getComponent(Position.class);
        this.render = entity.getComponent(Render.class);
        this.render.setActive(false);
        this.renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
    }

    static {
        // 新建帧缓冲用于离屏渲染 (暂未使用)

        IntBuffer intBuffer = BufferUtils.newIntBuffer(1);

        // 新建帧缓冲
        intBuffer.clear();
        Gdx.gl.glGenFramebuffers(1, intBuffer);
        frameBuffer = intBuffer.get(0);

        // 绑定帧缓冲
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        // 生成纹理
        Gdx.gl.glGenTextures(1, intBuffer);
        int texColorBuffer = intBuffer.get(0);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, texColorBuffer);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 800, 600, 0, GL_RGB, GL_UNSIGNED_BYTE, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);

        // 将它附加到当前绑定的帧缓冲对象
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texColorBuffer, 0);

        // 生成渲染缓冲对象
        Gdx.gl.glGenRenderbuffers(1, intBuffer);
        int rbo = intBuffer.get(0);
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        Gdx.gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, 800, 600);
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);

        // 将渲染缓冲对象附加到帧缓冲的深度和模板附件上
        Gdx.gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);

        // 检查帧缓冲完整性
        int status = Gdx.gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
        if (status != GL20.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("[ERROR] frame buffer not complete. status 0x" + Integer.toHexString(status));
            Gdx.app.exit();
        }
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void beforeRender(PerspectiveCamera cam) {
        render.setTransform(position);

        Vector3 tmpV = Vector3Pool.obtain();
        Matrix4 tmpM = Matrix4Pool.obtain();

        tmpM.set(position.getGlobalTransform()).inv().mul(targetTransform);

        this.camera.far = cam.far;
        this.camera.near = cam.near;
        this.camera.fieldOfView = cam.fieldOfView;
        this.camera.viewportWidth = cam.viewportWidth;
        this.camera.viewportHeight = cam.viewportHeight;
        this.camera.position.set(cam.position).mul(tmpM);
        this.camera.direction.set(cam.direction).rot(tmpM).nor();
        this.camera.up.set(cam.up).rot(tmpM);

        // 基于相机近剪裁平面实现传送门前方遮挡物体剔除 (问题: 不精确)
        this.camera.near = -radius + targetTransform.getTranslation(tmpV).sub(this.camera.position).dot(this.camera.direction);
        if (this.camera.near <= 0) {
            this.camera.near = 0.01f;
        }

        this.camera.update();

        Matrix4Pool.free(tmpM);
        Vector3Pool.free(tmpV);
    }

    @Override
    public void afterRender(PerspectiveCamera cam) {

//        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        // 准备工作
        Gdx.gl.glEnable(GL_STENCIL_TEST);
        Gdx.gl.glClear(GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        Gdx.gl.glEnable(GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL_CULL_FACE);

        // 渲染传送门轮廓至模板
        Gdx.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF); //所有片段都要写入模板缓冲
        Gdx.gl.glStencilMask(0xFF); // 设置模板缓冲为可写状态
        Gdx.gl.glColorMask(false, false, false, false);
        Gdx.gl.glFrontFace(GL_CW);
        batch.begin(cam);
        batch.render(render, shader);
        batch.end();
        Gdx.gl.glFrontFace(GL_CCW);
        Gdx.gl.glColorMask(true, true, true, true);

//        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
//        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        // 渲染传送门内场景
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT); // FIXME: 清空默认帧缓冲的深度缓存, 导致多于两个传送门的场景出现问题
        Gdx.gl.glStencilFunc(GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        renderSystem.render(camera);
        Gdx.gl.glStencilMask(0xFF);

        // 收尾工作
        Gdx.gl.glDisable(GL_STENCIL_TEST);

//        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

//    // ----- 基于斜近平面平截头体映射矩阵实现传送门前方遮挡物体剔除 (废弃: 未测试) ----- //
//
//    public void onPreRender() {
//        Vector4 cameraSpaceClipPlane = cameraSpacePlane(camera, new Vector3(0.0f, 0.0f, 0.0f), Vector3.Y, 1.0f);
//        calculateObliqueMatrix(camera.projection, cameraSpaceClipPlane);
//    }
//
//    public static Vector4 cameraSpacePlane(Camera cam, Vector3 pos, Vector3 normal, float sideSign) {
//        tmpV1.set(normal).scl(0.07f).add(pos);
//        tmpV1.mul(cam.view);
//        Vector3 point = new Vector3(0.0f, 0.0f, 0.0f).mul(tmpM.set(cam.view).inv());
//        tmpV1.sub(0.0f, point.y, 0.0f);
//        tmpV2.set(normal).mul(cam.view).nor().scl(sideSign);
//        return new Vector4(tmpV2.x, tmpV2.y, tmpV2.z, -tmpV1.dot(tmpV2));
//    }
//
//    private final static Vector3 tmpV1 = new Vector3();
//    private final static Vector3 tmpV2 = new Vector3();
//    private final static Matrix4 tmpM = new Matrix4();
//    public static void calculateObliqueMatrix(Matrix4 projection, Vector4 clipPlane) {
//        Vector4 q = new Vector4(
//                sgn(clipPlane.x),
//                sgn(clipPlane.y),
//                1.0f,
//                1.0f
//        ).mul(tmpM.set(projection).inv());
//        Vector4 c = clipPlane.scl(2.0f / clipPlane.dot(q));
//        // third row = clip plane - fourth row
//        projection.val[2] = c.x - projection.val[3];
//        projection.val[6] = c.y - projection.val[7];
//        projection.val[10] = c.z - projection.val[11];
//        projection.val[14] = c.w - projection.val[15];
//    }
//
//    public static float sgn(float a) {
//        if (a > 0.0f) return 1.0f;
//        if (a < 0.0f) return -1.0f;
//        return 0.0f;
//    }
//
//    public static class Vector4 {
//
//        private float x;
//        private float y;
//        private float z;
//        private float w;
//
//        public Vector4(float x, float y, float z, float w) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.w = w;
//        }
//
//        public Vector4 mul(Matrix4 matrix) {
//            final float[] l_mat = matrix.val;
//            x = x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + w * l_mat[Matrix4.M03];
//            y = x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + w * l_mat[Matrix4.M13];
//            x = x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + w * l_mat[Matrix4.M23];
//            w = x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + w * l_mat[Matrix4.M23];
//            return this;
//        }
//
//        public float dot(Vector4 vector) {
//            return x * vector.x + y * vector.y + z * vector.z + w * vector.w;
//        }
//
//        public Vector4 scl(float f) {
//            x = x * f;
//            y = x * f;
//            x = x * f;
//            w = x * f;
//            return this;
//        }
//    }
}
