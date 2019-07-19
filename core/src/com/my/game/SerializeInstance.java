package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.my.utils.world.mod.SerializeComponent;
import com.sun.istack.internal.NotNull;

public class SerializeInstance extends MyInstance {

    private static final Json JSON = new Json();

    protected SerializeComponent serializeComponent;

    public SerializeInstance(String name, @NotNull String serializeId) {
        super(name);
        serializeComponent = new SerializeComponent(new Serializer(), serializeId);
        addComponent("serialize", serializeComponent);
    }

    class Serializer implements SerializeComponent.Serializer {
        public String serialize() {
            Status status = new Status();
            btRigidBody body = phyComponent.getRigidBody();
            status.transform = body.getWorldTransform();
            status.activationState = body.getActivationState();
            status.linearVelocity = body.getLinearVelocity();
            status.angularVelocity = body.getLinearVelocity();
            return JSON.toJson(status);
        }
        public void deserialize(String data) {
            Status status = JSON.fromJson(Status.class, data);
            btRigidBody body = phyComponent.getRigidBody();
            body.activate();
            body.proceedToTransform(status.transform);
            body.forceActivationState(status.activationState);
            body.setLinearVelocity(status.linearVelocity);
            body.setAngularVelocity(status.angularVelocity);
        }
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
