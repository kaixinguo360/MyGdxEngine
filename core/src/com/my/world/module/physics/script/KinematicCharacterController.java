package com.my.world.module.physics.script;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.script.ScriptSystem;

public class KinematicCharacterController extends ActivatableComponent implements ScriptSystem.OnStart, ScriptSystem.OnRemoved, ScriptSystem.OnUpdate, Disposable {

    @Config(type = Config.Type.Asset)
    public TemplateRigidBody shape;

    @Config public float stepHeight = 0.35f;
    @Config public float velocity = 1f;
    @Config public float mass = 1f;

    protected Position position;
    protected PhysicsSystem physicsSystem;
    protected btDynamicsWorld dynamicsWorld;

    protected btPairCachingGhostObject ghostObject;
    protected btKinematicCharacterController characterController;

    @Config public final Vector3 currentVelocity = new Vector3();

    public void syncTransformFromEntity() {
        ghostObject.setWorldTransform(position.getLocalTransform());
    }

    public void syncTransformFromDynamicsWorld() {
        ghostObject.getWorldTransform(position.getLocalTransform());
    }

    @Override
    public void start(Scene scene, Entity entity) {
        Matrix4 tmp = Matrix4Pool.obtain();

        position = entity.getComponent(Position.class);
        physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
        dynamicsWorld = physicsSystem.getDynamicsWorld();

        ghostObject = new btPairCachingGhostObject();
//        ghostObject.setWorldTransform(position.getGlobalTransform()); // Fix Bug #5017
        ghostObject.setCollisionShape(shape.shape);
        ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK | btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        ghostObject.userData = entity;
        position.setDisableInherit(true);

        characterController = new btKinematicCharacterController(ghostObject, (btConvexShape) shape.shape, stepHeight, Vector3.Y);
        characterController.setGravity(new Vector3(0, -30f * mass, 0));

        // Bug of LibGdx.Bullet #5017
        // Creating a btKinematicCharacterController using the constructor
        // that gets the up axis then setting gravity to a scaled version
        // of "up" causes the ghostObject to rotate.
        // Reference: https://github.com/libgdx/libgdx/issues/5017
        ghostObject.setWorldTransform(position.getGlobalTransform()); // Fix Bug #5017

        dynamicsWorld.addCollisionObject(ghostObject,
                (short) btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
                (short) btBroadphaseProxy.CollisionFilterGroups.AllFilter);
        dynamicsWorld.addAction(characterController);

        Matrix4Pool.free(tmp);
    }

    @Override
    public void removed(Scene scene, Entity entity) {
        dynamicsWorld.removeAction(characterController);
        dynamicsWorld.removeCollisionObject(ghostObject);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        float deltaTime = scene.getTimeManager().getDeltaTime();
        characterController.setWalkDirection(currentVelocity.rot(position.getGlobalTransform()).scl(deltaTime));
        currentVelocity.setZero();
        position.getLocalTransform().set(ghostObject.getWorldTransform());
    }

    @Override
    public void dispose() {
        position = null;
        physicsSystem = null;
        dynamicsWorld = null;
        if (ghostObject != null) {
            ghostObject.dispose();
            ghostObject = null;
        }
        if (characterController != null) {
            characterController.dispose();
            characterController = null;
        }
    }
}
