package com.my.world.module.physics.script;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.my.world.core.Config;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.physics.BasePhysicsBody;
import com.my.world.module.physics.TemplateRigidBody;

public class KinematicCharacterController extends BasePhysicsBody implements Disposable {

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    @Config(type = Config.Type.Asset)
    public TemplateRigidBody shape;

    @Config public float stepHeight = 0.35f;
    @Config public float mass = 1f;

    public btPairCachingGhostObject ghostObject;
    public btKinematicCharacterController characterController;

    protected void createGhostObject() {
        Matrix4 tmp = Matrix4Pool.obtain();

        ghostObject = new btPairCachingGhostObject();
//        ghostObject.setWorldTransform(position.getGlobalTransform()); // Fix Bug #5017
        ghostObject.setCollisionShape(shape.shape);
        ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK | btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        ghostObject.userData = this;

        characterController = new btKinematicCharacterController(ghostObject, (btConvexShape) shape.shape, stepHeight, Vector3.Y);
        characterController.setGravity(new Vector3(0, -30f * mass, 0));

        // Bug of LibGdx.Bullet #5017
        // Creating a btKinematicCharacterController using the constructor
        // that gets the up axis then setting gravity to a scaled version
        // of "up" causes the ghostObject to rotate.
        // Reference: https://github.com/libgdx/libgdx/issues/5017
        ghostObject.setWorldTransform(position.getGlobalTransform()); // Fix Bug #5017

        Matrix4Pool.free(tmp);
    }

    @Override
    public void enterWorld() {
        super.enterWorld();
        if (!position.isDisableInherit()) {
            position.disableInherit();
        }
        if (ghostObject == null) {
            createGhostObject();
        }
        dynamicsWorld.addCollisionObject(ghostObject,
                (short) btBroadphaseProxy.CollisionFilterGroups.CharacterFilter | group,
                mask);
        dynamicsWorld.addAction(characterController);
    }

    @Override
    public void leaveWorld() {
        dynamicsWorld.removeAction(characterController);
        dynamicsWorld.removeCollisionObject(ghostObject);
        super.leaveWorld();
    }

    @Override
    public void syncTransformFromEntity() {
        ghostObject.setWorldTransform(position.getGlobalTransform());
    }

    @Override
    public void syncTransformFromWorld() {
        position.setGlobalTransform(ghostObject.getWorldTransform());
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
