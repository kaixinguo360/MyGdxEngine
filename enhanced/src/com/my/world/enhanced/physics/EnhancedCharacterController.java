package com.my.world.enhanced.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.script.KinematicCharacterController;
import com.my.world.module.script.ScriptSystem;

public class EnhancedCharacterController extends KinematicCharacterController implements ScriptSystem.OnUpdate, InputSystem.OnKeyDown, InputSystem.OnMouseMoved {

    @Config public int keyUp = Input.Keys.UP;
    @Config public int keyDown = Input.Keys.DOWN;
    @Config public int keyLeft = Input.Keys.LEFT;
    @Config public int keyRight = Input.Keys.RIGHT;
    @Config public int keyJump = Input.Keys.SPACE;

    @Config public float yawRate = 1f;
    @Config public float acceleration = 1;
    @Config public float damping = 1;
    @Config public float minVelocity = 0;
    @Config public float maxVelocity = 1;
    @Config public float velocity = 1f;
    @Config public final Vector3 currentVelocity = new Vector3();

    @Config public float jumpCD = 1f;
    protected float currentJumpCD = 0;

    @Override
    public void update(Scene scene, Entity entity) {
        float deltaTime = scene.getTimeManager().getDeltaTime();

        if (Gdx.input.isKeyPressed(keyUp) || Gdx.input.isKeyPressed(keyDown)
                || Gdx.input.isKeyPressed(keyLeft) || Gdx.input.isKeyPressed(keyRight)) {
            velocity += acceleration * deltaTime;
            if (Gdx.input.isKeyPressed(keyUp)) currentVelocity.z = velocity;
            if (Gdx.input.isKeyPressed(keyDown)) currentVelocity.z = -velocity;
            if (Gdx.input.isKeyPressed(keyLeft)) currentVelocity.x = velocity;
            if (Gdx.input.isKeyPressed(keyRight)) currentVelocity.x = -velocity;
        } else {
            velocity -= damping * deltaTime;
        }

        velocity = MathUtils.clamp(velocity, minVelocity, maxVelocity);

        if (currentJumpCD > 0) {
            currentJumpCD -= deltaTime;
        }

        characterController.setWalkDirection(currentVelocity.rot(position.getGlobalTransform()).scl(deltaTime));
        currentVelocity.setZero();
        position.getLocalTransform().set(ghostObject.getWorldTransform());
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
        syncTransformFromWorld();
        position.getLocalTransform().rotate(Vector3.Y, -Gdx.input.getDeltaX() * 0.1f * yawRate);
        syncTransformFromEntity();
    }

}
