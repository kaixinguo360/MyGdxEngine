package com.my.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.my.game.base.BaseGame;

public class MyGame extends BaseGame {

    private SpriteBatch batch;
    private Texture img;

    @Override
    public void create() {
        super.create();
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
    }

    @Override
    protected void myRender() {
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
        super.dispose();
    }
}
