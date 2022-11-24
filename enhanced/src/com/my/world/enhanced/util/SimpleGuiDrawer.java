package com.my.world.enhanced.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;

import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

public class SimpleGuiDrawer extends ShapeRenderer {

    public static final Matrix4 tmpM = new Matrix4();

    /**
     * Normalized origin coordinates, (0, 0) is bottom left, (0.5, 0.5) is center
     */
    public final Vector2 origin = new Vector2();

    protected float screenWidth = 0;
    protected float screenHeight = 0;

    @Getter
    public final Matrix4 projectionMatrix = new Matrix4();

    // ----- Override ----- //

    @Override
    public void begin() {
        begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void begin(ShapeRenderer.ShapeType type) {
        syncScreenSize();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        setProjectionMatrix(projectionMatrix);
        setTransformMatrix(tmpM.idt());
        super.begin(type);
    }

    // ----- Utils ----- //

    public void syncScreenSize() {
        float newScreenWidth = Gdx.graphics.getWidth();
        float newScreenHeight = Gdx.graphics.getHeight();
        if (newScreenWidth == screenWidth && newScreenHeight == screenHeight) return;
        screenWidth = newScreenWidth;
        screenHeight = newScreenHeight;
        projectionMatrix.idt().scl(2f / screenWidth, 2f / screenHeight, 0);
        projectionMatrix.translate(-screenWidth / 2f, -screenHeight / 2f, 0);
        projectionMatrix.translate(origin.x * screenWidth, origin.y * screenHeight, 0);
    }

    public float width() {
        return width(1);
    }

    public float width(float percentage) {
        return screenWidth * percentage;
    }

    public float height() {
        return height(1);
    }

    public float height(float percentage) {
        return screenHeight * percentage;
    }

    // ----- Draw ----- //

    public void square(float x, float y, float sideLength) {
        rectangle(x, y, sideLength, sideLength);
    }

    public void rectangle(float x, float y, float width, float height) {
        rect(x - width / 2, y - height / 2, width, height);
    }
}
