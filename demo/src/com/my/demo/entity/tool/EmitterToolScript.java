package com.my.demo.entity.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.TimeManager;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseComponent;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;

public class EmitterToolScript extends BaseComponent implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, InputSystem.OnTouchDown, InputSystem.OnKeyDown {

    @Config public String parentId;
    @Config public EntityBuilder builder;
    @Config public Matrix4 offset;
    @Config public Vector3 impulse;
    @Config public Vector3 velocity;

    @Config public int fireKey = Input.Keys.J;
    @Config public int fireButton = Input.Buttons.LEFT;

    @Config public boolean burstMode = false;
    @Config public float RoF = 0;
    public float CD = 0;

    protected Scene scene;
    protected TimeManager timeManager;
    protected Position position;


    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
        this.timeManager = scene.getTimeManager();
        Entity parent = scene.getEntityManager().findEntityById(parentId);
        if (parent == null) throw new RuntimeException("No such entity: id=" + parentId);
        this.position = parent.getComponent(Position.class);
        if (this.position == null) throw new RuntimeException("No position component found in this entity: id=" + parentId);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (CD > 0) {
            CD -= timeManager.getDeltaTime();
            CD = CD < 0 ? 0 : CD;
        }
        if (!burstMode) return;
        if (Gdx.input.isButtonPressed(fireButton) || Gdx.input.isKeyPressed(fireKey)) fire();
    }

    @Override
    public void touchDown(int screenX, int screenY, int pointer, int button) {
        if (burstMode) return;
        if (button == fireButton) fire();
    }

    @Override
    public void keyDown(int keycode) {
        if (burstMode) return;
        if (keycode == fireKey) fire();
    }

    public void fire() {
        if (CD > 0) return;
        CD = RoF;
        Entity entity = builder.build(scene);

        Position position = entity.getComponent(Position.class);
        if (position == null) return;
        if (offset != null) position.setGlobalTransform(m -> m.set(this.position.getGlobalTransform()).mul(offset));

        RigidBody rigidBody = entity.getComponent(RigidBody.class);
        if (rigidBody == null) return;
        Vector3 tmpV = Vector3Pool.obtain();
        if (impulse != null) rigidBody.body.applyImpulse(tmpV.set(impulse).rot(position.getGlobalTransform()), Vector3.Zero);
        if (velocity != null) rigidBody.body.setLinearVelocity(tmpV.set(velocity).rot(position.getGlobalTransform()));
        Vector3Pool.free(tmpV);
    }
}
