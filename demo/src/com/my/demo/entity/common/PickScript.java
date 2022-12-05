package com.my.demo.entity.common;

import com.badlogic.gdx.Gdx;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.Camera;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.function.Consumer;
import java.util.function.Function;

public class PickScript extends ActivatableComponent implements ScriptSystem.OnStart {

    @Config public String cameraId;

    @Config public float maxLength = 500;
    @Config public boolean fixedPosition = true;
    @Config public Float defaultX;
    @Config public Float defaultY;

    @Config public OnPick onPick;
    @Config public Filter filter;

    protected PhysicsSystem physicsSystem;
    protected Entity cameraEntity;
    protected Camera camera;

    @Override
    public void start(Scene scene, Entity entity) {
        this.physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
        cameraEntity = scene.getEntityManager().findEntityById(cameraId);
        if (cameraEntity == null) throw new RuntimeException("No such entity: id=" + cameraId);
        camera = cameraEntity.getComponent(Camera.class);
        if (camera == null) throw new RuntimeException("This entity don't have a Camera component: " + entity.getId());
    }

    // ----- Pick ----- //

    public void pick() {
        pick(getDefaultX(), getDefaultY());
    }

    public void pick(float screenX, float screenY) {
        Entity pickedEntity = pickEntity(screenX, screenY);
        if (pickedEntity != null) {
            onPick(pickedEntity);
        }
    }

    // ----- Pick Entity ----- //

    public Entity pickEntity() {
        return pickEntity(getDefaultX(), getDefaultY());
    }

    public Entity pickEntity(float screenX, float screenY) {
        if (!camera.isActive()) return null;
        if (fixedPosition) {
            screenX = getDefaultX();
            screenY = getDefaultY();
        }
        Entity entity = physicsSystem.pick(camera.getCamera(), screenX, screenY, maxLength);
        if (entity != null && filter(entity)) return entity;
        return null;
    }

    // ----- Default ----- //

    protected int getDefaultX() {
        return (int) (defaultX != null ? defaultX : Gdx.graphics.getWidth() * (camera.getStartX() + (camera.getEndX() - camera.getStartX()) / 2));
    }

    protected int getDefaultY() {
        return (int) (defaultY != null ? defaultY : Gdx.graphics.getHeight() * (camera.getStartY() + (camera.getEndY() - camera.getStartY()) / 2));
    }

    // ----- OnPick ----- //

    public void onPick(Entity entity) {
        if (onPick != null) {
            onPick.accept(entity);
        } else {
            System.out.println("Pick: " + entity.getId());
        }
    }

    public interface OnPick extends Consumer<Entity>, Configurable {}

    // ----- Filter ----- //

    protected boolean filter(Entity entity) {
        return filter == null || filter.apply(entity);
    }

    public interface Filter extends Function<Entity, Boolean>, Configurable {}
}
