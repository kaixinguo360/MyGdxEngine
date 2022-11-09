package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.input.InputSystem;

import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

public class EnhancedChoppingScript extends ChoppingScript implements InputSystem.OnTouchDragged, CameraSystem.AfterRender {

    protected ShapeRenderer shapeRenderer;

    protected int originalX;
    protected int originalY;
    protected int currentX;
    protected int currentY;
    protected final Vector3 originalRotation = new Vector3();

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void touchDown(int screenX, int screenY, int pointer, int button) {
        super.touchDown(screenX, screenY, pointer, button);
        this.originalX = screenX;
        this.originalY = screenY;
        this.currentX = screenX;
        this.currentY = screenY;
        cutter.position.decompose();
        originalRotation.set(cutter.position.rotation);
    }

    public static final Vector2 tmpV2 = new Vector2();

    @Override
    public void touchDragged(int screenX, int screenY, int pointer) {
        if (isChopping) {
            currentX = screenX;
            currentY = screenY;
            float angle = tmpV2.set(currentX - originalX, currentY - originalY).angleDeg();
            angle = -((angle + 180) % 360 - 180);
            cutter.position.decompose();
            cutter.position.rotation.set(originalRotation);
            cutter.position.rotation.z = angle;
            cutter.position.compose();
        }
    }

    @Override
    public void touchUp(int screenX, int screenY, int pointer, int button) {
        super.touchUp(screenX, screenY, pointer, button);
        cutter.position.decompose();
        cutter.position.rotation.set(originalRotation);
        cutter.position.compose();
    }

    @Override
    public void afterRender(Camera cam) {
        if (isChopping) {
            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            float centerX = width / 2;
            float centerY = height / 2;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            // Reset ShapeRenderer
            shapeRenderer.setProjectionMatrix(new Matrix4().scl(2f / Gdx.graphics.getWidth(), -2f / Gdx.graphics.getHeight(), 0).translate(-Gdx.graphics.getWidth() / 2f, -Gdx.graphics.getHeight() / 2f, 0));
            shapeRenderer.setTransformMatrix(new Matrix4());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Draw Square
            shapeRenderer.setColor(1, 1, 1, 0.8f);
            shapeRenderer.rectLine(centerX + (currentX - originalX),
                    centerY + (currentY - originalY),
                    centerX - (currentX - originalX),
                    centerY - (currentY - originalY),
                    2);

            // Draw Cross
            shapeRenderer.setColor(0, 0, 0, 0.2f);
            shapeRenderer.rectLine(centerX - 100, centerY, centerX + 100, centerY, 2);
            shapeRenderer.rectLine(centerX, centerY - 100, centerX, centerY + 100, 2);
            shapeRenderer.end();
        }
    }
}
