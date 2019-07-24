package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.Entity;
import com.my.utils.world.com.*;

public class MyInstance extends Entity {

    protected Position position;
    protected Render render;
    protected RigidBody rigidBody;
    protected Serialization serialization;

    public MyInstance() {}

    public MyInstance(String name) {
        position = add(Position.class, new Position());
        render = add(Render.class, Render.get(name, position));
        rigidBody = add(RigidBody.class, RigidBody.get(name));
    }

    public MyInstance(String name, String group) {
        this(name);
        serialization = new Serialization();
        serialization.group = group;
        serialization.serializerId = name;
        add(Serialization.class, serialization);
    }

    public MyInstance(String name, String group, Motion motion) {
        this(name, group);
        add(Motion.class, motion);
    }

    public void setTransform(Matrix4 transform) {
        get(Position.class).transform.set(transform);
        if (contain(RigidBody.class)) get(RigidBody.class).body.proceedToTransform(transform);
    }

    @Override
    public void dispose() {
        if (contain(RigidBody.class)) get(RigidBody.class).body.dispose();
    }
}
