package com.my.demo.entity.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.CollisionConstants;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.QuaternionPool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.RigidBody;

public class DragPickScript extends PickScript implements CameraSystem.AfterRender, InputSystem.OnTouchDragged,
        InputSystem.OnTouchDown, InputSystem.OnTouchUp, InputSystem.OnKeyDown, InputSystem.OnKeyUp {

    @Config
    public int pickKey = Input.Keys.F;

    @Config
    public int pickButton = Input.Buttons.LEFT;

    @Config
    public float rate = 1f;

    protected Entity pickedEntity;
    protected RigidBody rigidBody;
    protected Position position;
    protected Matrix4 originalTransform = new Matrix4();
    protected Matrix4 currentTransform = new Matrix4();
    protected boolean originalIsKinematic;
    protected float originalScreenX;
    protected float originalScreenY;
    private ShapeRenderer shapeRenderer;

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == pickButton) pickDown(screenX, screenY);
    }

    @Override
    public void touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == pickButton) pickUp(screenX, screenY);
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == pickKey) pickDown(getDefaultX(), getDefaultY());
    }

    @Override
    public void keyUp(int keycode) {
        if (keycode == pickKey) pickUp(getDefaultX(), getDefaultY());
    }

    public void pickDown(int screenX, int screenY) {
        originalScreenX = screenX;
        originalScreenY = screenY;
        pickedEntity = pickEntity(screenX, screenY);
        if (pickedEntity != null) {
            position = pickedEntity.getComponent(Position.class);
            position.getGlobalTransform(originalTransform);
            currentTransform.set(originalTransform);
            rigidBody = pickedEntity.getComponent(RigidBody.class);
            rigidBody.collisionFlags = rigidBody.body.getCollisionFlags();
            originalIsKinematic = rigidBody.isKinematic;
            rigidBody.isKinematic = true;
            rigidBody.reenterWorld();
            rigidBody.body.setActivationState(CollisionConstants.DISABLE_DEACTIVATION);
        }
    }

    public void pickUp(int screenX, int screenY) {
        if (pickedEntity != null) {
            rigidBody.isKinematic = originalIsKinematic;
            originalIsKinematic = false;
            rigidBody.reenterWorld();
            rigidBody.body.setActivationState(CollisionConstants.ACTIVE_TAG);
            rigidBody.body.activate();
            originalTransform.idt();
            position = null;
            rigidBody = null;
            pickedEntity = null;
        }
        originalScreenX = 0;
        originalScreenY = 0;
    }

    @Override
    public void touchDragged(int screenX, int screenY, int pointer) {
        if (pickedEntity != null) {
            Vector3 tmpV1 = Vector3Pool.obtain();
            Vector3 tmpV2 = Vector3Pool.obtain();
            Quaternion tmpQ = QuaternionPool.obtain();
            Camera cam = camera.getCamera();

            originalTransform.getTranslation(tmpV1);
            originalTransform.getScale(tmpV2);
            originalTransform.getRotation(tmpQ);

            cam.project(tmpV1);
            tmpV1.y = Gdx.graphics.getHeight() -tmpV1.y;

            tmpV1.x += (screenX - originalScreenX) * rate;
            tmpV1.y += (screenY - originalScreenY) * rate;

            cam.unproject(tmpV1);

            currentTransform.set(tmpV1, tmpQ, tmpV2);

            position.setGlobalTransform(currentTransform);

            Vector3Pool.free(tmpV1);
        }
    }

    @Override
    public void afterRender(Camera cam) {
        if (pickedEntity != null) {
            Vector3 tmpV = Vector3Pool.obtain();

            Gdx.gl.glEnable(GL20.GL_BLEND);

            currentTransform.getTranslation(tmpV);
            camera.getCamera().project(tmpV);
            System.out.println(tmpV);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 1, 1, 1);
            shapeRenderer.circle(tmpV.x, tmpV.y, 10);
            shapeRenderer.end();

            Vector3Pool.free(tmpV);
        }
    }
}
