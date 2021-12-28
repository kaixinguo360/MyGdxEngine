package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import com.my.utils.world.Prefab;
import com.my.utils.world.Scene;
import com.my.utils.world.com.Camera;
import com.my.utils.world.sys.KeyInputSystem;
import com.my.utils.world.sys.ScriptSystem;

public class GunScript extends EmitterScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    private GunController gunController_Y;
    private GunController gunController_X;

    @Config public Prefab bulletPrefab;
    @Config public Vector3 bulletVelocity = new Vector3(0, 0, -2000);
    private final static Vector3 bulletOffset = new Vector3(0, 0, -5);

    @Config public Prefab bombPrefab;
    @Config public Vector3 bombVelocity = new Vector3(0, 0, -100);
    private final static Vector3 bombOffset = new Vector3(0, 0, -5);

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);

        main = entity.findChildByName("barrel");
        Entity rotate_Y = entity.findChildByName("rotate_Y");
        Entity rotate_X = entity.findChildByName("rotate_X");
        parts.add(rotate_X);
        parts.add(rotate_Y);

        this.camera = main.getComponent(Camera.class);
        if (rotate_Y.contains(GunController.class)) gunController_Y = rotate_Y.getComponent(GunController.class);
        if (rotate_X.contains(GunController.class)) gunController_X = rotate_X.getComponent(GunController.class);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (camera != null && !disabled) {
            float v = 0.025f;
            if (gunController_Y != null && gunController_X != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                    getMainBody().activate();
                    gunController_X.target += -v;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                    getMainBody().activate();
                    gunController_X.target += v;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    getMainBody().activate();
                    gunController_Y.target += v;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    getMainBody().activate();
                    gunController_Y.target += -v;
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire(bulletPrefab, bulletVelocity, bulletOffset, (float) Math.random());
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
        }
    }

    @Override
    public void keyDown(Scene scene, Entity entity, int keycode) {
        if (camera == null) return;
        if (keycode == Input.Keys.TAB) changeCamera();
        if (keycode == Input.Keys.SHIFT_LEFT && !disabled) changeCameraFollowType();
        if (camera != null && !disabled) {
            if (keycode == Input.Keys.K) fire(bombPrefab, bombVelocity, bombOffset, (float) Math.random());
        }
    }
}
