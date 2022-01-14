package com.my.world.module.physics.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;

public class EnhancedCharacterController extends KinematicCharacterController implements InputSystem.OnKeyDown, InputSystem.OnMouseMoved {

    @Config public int keyUp = Input.Keys.UP;
    @Config public int keyDown = Input.Keys.DOWN;
    @Config public int keyLeft = Input.Keys.LEFT;
    @Config public int keyRight = Input.Keys.RIGHT;
    @Config public int keyJump = Input.Keys.SPACE;

    @Config public float yawRate = 1f;

    @Config public float jumpCD = 1f;
    protected float currentJumpCD = 0;

    @Override
    public void update(Scene scene, Entity entity) {
        if (Gdx.input.isKeyPressed(keyUp)) currentVelocity.z = velocity;
        if (Gdx.input.isKeyPressed(keyDown)) currentVelocity.z = -velocity;
        if (Gdx.input.isKeyPressed(keyLeft)) currentVelocity.x = velocity;
        if (Gdx.input.isKeyPressed(keyRight)) currentVelocity.x = -velocity;

        if (currentJumpCD > 0) {
            currentJumpCD -= Gdx.graphics.getDeltaTime();
        }

        super.update(scene, entity);
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == keyJump) {
            if (currentJumpCD <= 0) {
                characterController.jump(new Vector3(0, 10, 0));
                currentJumpCD = jumpCD;
            }
        }
    }

    @Override
    public void mouseMoved(int screenX, int screenY) {
        position.getLocalTransform().rotate(Vector3.Y, -Gdx.input.getDeltaX() * 0.1f * yawRate);
        ghostObject.setWorldTransform(position.getGlobalTransform());
    }
}
