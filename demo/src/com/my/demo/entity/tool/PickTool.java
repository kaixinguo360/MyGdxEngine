package com.my.demo.entity.tool;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.CollisionConstants;
import com.my.demo.entity.common.PickScript;
import com.my.demo.entity.common.SightScript;
import com.my.demo.entity.object.CameraEntity;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;

public class PickTool extends EnhancedEntity {

    public PickTool(CameraEntity camera) {
        setName("PickTool");
        PickToolScript script = addComponent(new PickToolScript());
        script.cameraId = camera.getId();
        script.filter = entity -> !"ground".equals(entity.getName());
        addComponent(new SightScript());
        setParent(camera);
    }

    public static class PickToolScript extends PickScript implements ScriptSystem.OnUpdate, InputSystem.OnTouchDown, InputSystem.OnTouchUp, InputSystem.OnKeyDown, InputSystem.OnKeyUp {

        @Config public int pickKey = Input.Keys.F;
        @Config public int pickButton = Input.Buttons.LEFT;

        protected Position cameraPosition;
        protected Entity pickedEntity;
        protected Position position;
        protected Matrix4 offset = new Matrix4();
        protected RigidBody rigidBody;
        protected boolean originalIsKinematic;

        @Override
        public void start(Scene scene, Entity entity) {
            super.start(scene, entity);
            cameraPosition = cameraEntity.getComponent(Position.class);
            if (cameraPosition == null) throw new RuntimeException("This entity don't have a Position component: " + entity.getId());
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
            if (pickedEntity != null) return;
            pickedEntity = pickEntity(screenX, screenY);
            if (pickedEntity == null) return;
            position = pickedEntity.getComponent(Position.class);
            if (position != null) {
                offset.set(cameraPosition.getGlobalTransform()).inv().mul(position.getGlobalTransform());
            }
            rigidBody = pickedEntity.getComponent(RigidBody.class);
            if (rigidBody != null && rigidBody.isEnteredWorld()) {
                rigidBody.collisionFlags = rigidBody.body.getCollisionFlags();
                originalIsKinematic = rigidBody.isKinematic;
                rigidBody.isKinematic = true;
                rigidBody.reenterWorld();
                rigidBody.body.setActivationState(CollisionConstants.DISABLE_DEACTIVATION);
            }
        }

        public void pickUp(int screenX, int screenY) {
            if (pickedEntity == null) return;
            if (rigidBody != null && rigidBody.isEnteredWorld()) {
                rigidBody.isKinematic = originalIsKinematic;
                rigidBody.reenterWorld();
                rigidBody.body.setActivationState(CollisionConstants.ACTIVE_TAG);
                rigidBody.body.activate();
            }
            originalIsKinematic = false;
            offset.idt();
            position = null;
            rigidBody = null;
            pickedEntity = null;
        }

        @Override
        public void update(Scene scene, Entity entity) {
            if (pickedEntity == null) return;
            position.setGlobalTransform(m -> m.set(cameraPosition.getGlobalTransform()).mul(offset));
        }
    }
}
