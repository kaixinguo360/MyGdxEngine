package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Id;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.Serialization;

public class Serializer implements Serialization.Serializer {

    private static final Json JSON = new Json();
    private World world;

    public Serializer(World world) {
        this.world = world;
    }

    public String serialize(Entity entity) {
        RigidBody rigidBody = entity.get(RigidBody.class);
        btRigidBody body = rigidBody.body;

        Status status = new Status();
        status.transform = body.getWorldTransform();
        status.activationState = body.getActivationState();
        status.linearVelocity = body.getLinearVelocity();
        status.angularVelocity = body.getLinearVelocity();
        return JSON.toJson(status);
    }

    @Override
    public void add(String id, String group, String serializerId, String data) {
        try {
            Entity entity = world.addEntity(id, new MyInstance(serializerId, group));
            update(entity, data);
        } catch (RuntimeException ignored) {}
    }

    public void update(Entity entity, String data) {
        btRigidBody body = entity.get(RigidBody.class).body;

        Status status = JSON.fromJson(Status.class, data);
        body.activate();
        body.proceedToTransform(status.transform);
        body.forceActivationState(status.activationState);
        body.setLinearVelocity(status.linearVelocity);
        body.setAngularVelocity(status.angularVelocity);
    }

    @Override
    public void remove(Entity entity) {
        world.removeEntity(entity.get(Id.class).id);
    }

    static class Status implements Json.Serializable {
        Matrix4 transform;
        int activationState;
        Vector3 linearVelocity;
        Vector3 angularVelocity;

        public void write(Json json) {
            json.writeValue("transform", transform);
            json.writeValue("activationState", activationState);
            json.writeValue("linearVelocity", linearVelocity);
            json.writeValue("angularVelocity", angularVelocity);
        }

        public void read(Json json, JsonValue jsonData) {
            transform = json.readValue("transform", Matrix4.class, jsonData);
            activationState = json.readValue("activationState", Integer.class, jsonData);
            linearVelocity = json.readValue("linearVelocity", Vector3.class, jsonData);
            angularVelocity = json.readValue("angularVelocity", Vector3.class, jsonData);
        }
    }
}

