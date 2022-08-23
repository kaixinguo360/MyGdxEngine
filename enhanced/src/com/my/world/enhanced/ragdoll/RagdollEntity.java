package com.my.world.enhanced.ragdoll;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.Constraint;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.ConeTwistConstraint;
import com.my.world.module.render.BaseRender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class RagdollEntity extends EnhancedEntity {

    public static final int RAGDOLL_GROUP = 1 << 10;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Param {
        public Model model;
        public String rootNodeId;
        public String rootBodyId;
        public String rootBoneId;
        @Builder.Default public boolean parentTransform = false;
        @Builder.Default public boolean mergeTransform = true;
        @Builder.Default public boolean isKinematic = false;
        @Builder.Default public float mass = 50;
        @Builder.Default public float defaultBoneRadius = 0.5f;
        @Builder.Default public float defaultBoneHeight = 1f;
        @Builder.Default public float defaultBoneMass = 10f;
        @Builder.Default public float defaultSwingSpanLimit = 45f;
        @Builder.Default public float defaultTwistSpanLimit = 45f;
    }

    public final ModelInstance modelInstance;
    public final BoneEntity rootBone;
    public final List<BoneEntity> bones = new ArrayList<>();

    public final BaseRender render;
    public final TemplateRigidBody rigidBody;
    public final PhysicsSystem.OnFixedUpdate onUpdateScript;
    public final InputSystem.OnKeyDown onKeyDownScript;

    public RagdollEntity(Param p) {

        Node node = p.model.getNode(p.rootNodeId);
        if (node == null) throw new RuntimeException("No such node: " + p.rootNodeId);

        node.calculateLocalTransform();
        node.calculateWorldTransform();

        // Root Entity
        String id = node.id.replaceAll("^([^\\[]*)\\[.*$", "$1");
        setName(id);
        // Set Position
        if (p.mergeTransform) {
            if (p.parentTransform) {
                position.setLocalTransform(node.calculateWorldTransform());
            } else {
                position.setLocalTransform(node.calculateLocalTransform());
            }
        }
        // Add Render
        modelInstance = new ModelInstance(p.model, p.rootNodeId, true, false, true);
        render = addComponent(new BaseRender(modelInstance));
        render.isAlwaysVisible = true;
        // Add RigidBody
        Node rootBodyNode = modelInstance.getNode(p.rootBodyId);
        btCollisionShape shape;
        if (rootBodyNode != null) {
            shape = MeshUtil.getConvexHullShape(rootBodyNode, rootBodyNode.calculateWorldTransform());
            rootBodyNode.getParent().removeChild(rootBodyNode);
        } else {
            Vector3 tmpV = Vector3Pool.obtain();
            Matrix4 tmpM = Matrix4Pool.obtain();
            btCompoundShape compoundShape = new btCompoundShape();
            compoundShape.addChildShape(
                    tmpM.setToTranslation(render.center),
                    new btBoxShape(tmpV.set(render.dimensions).scl(0.5f))
            );
            shape = compoundShape;
            Matrix4Pool.free(tmpM);
            Vector3Pool.free(tmpV);
        }
        rigidBody = addComponent(new TemplateRigidBody(shape, p.mass));
        rigidBody.group = RAGDOLL_GROUP;
        rigidBody.isKinematic = p.isKinematic;
        rigidBody.autoConvertToLocalTransform = true;
        // Add OnUpdate Script
        onUpdateScript = addComponent((scene1, world1, entity1) -> {
            for (BoneEntity bone : bones) {
                bone.calculateBoneGlobalTransform();
            }
            for (Node n : render.modelInstance.nodes) {
                n.calculateBoneTransforms(true);
            }
        });
        // Add OnKeyDown Script
        onKeyDownScript = addComponent((InputSystem.OnKeyDown) new InputSystem.OnKeyDown() {

            private boolean isKinematic = true;
            private final Matrix4 rootOffset = new Matrix4();

            @Override
            public void keyDown(int keycode) {
                if (keycode == Input.Keys.X && isKinematic) {
                    rigidBody.setActive(false);
                    rootBone.position.getLocalTransform(rootOffset);
                    for (EnhancedEntity entity1 : bones) {
                        RigidBody rigidBody = entity1.getComponent(RigidBody.class);
                        rigidBody.isKinematic = false;
                        rigidBody.reenterWorld();
                        rigidBody.body.setActivationState(CollisionConstants.WANTS_DEACTIVATION);
                        rigidBody.body.activate();
                    }
                    isKinematic = false;
                }
                if (keycode == Input.Keys.C && !isKinematic) {
                    rigidBody.setActive(true);
                    position.setGlobalTransform(rootOffset.inv().mulLeft(rootBone.position.getGlobalTransform()));
                    for (EnhancedEntity entity1 : bones) {
                        RigidBody rigidBody = entity1.getComponent(RigidBody.class);
                        rigidBody.isKinematic = true;
                        rigidBody.reenterWorld();
                        rigidBody.body.setActivationState(CollisionConstants.DISABLE_DEACTIVATION);
                    }
                    isKinematic = true;
                }
            }
        });

        // Root Bone Entity
        Node rootBoneNode = modelInstance.getNode(p.rootBoneId);
        if (rootBoneNode == null) {
            throw new RuntimeException("No such node: " + p.rootBoneId);
        }
        rootBone = createBoneRecursive(rootBoneNode, null, p);
    }

    protected final BoneEntity createBoneRecursive(Node node, BoneEntity parentBone, Param p) {

        // Current Node
        BoneEntity entity = createBone(node, parentBone, p);
        if (parentBone != null) {
            entity.setParent(parentBone);
        } else {
            entity.setParent(this);
        }
        addBone(entity);

        // Children Node
        for (Node childNode : node.getChildren()) {
            createBoneRecursive(childNode, entity, p);
        }

        return entity;
    }

    // ----- Utils ----- //

    protected <T extends BoneEntity> T addBone(T bone) {
        addEntity(bone);
        bones.add(bone);
        return bone;
    }

    public BoneEntity findBone(String name) {
        return findBone(name, false);
    }

    public BoneEntity findBone(String name, boolean notNull) {
        if (name == null) throw new RuntimeException("Name of bone can not be null");
        for (BoneEntity bone : bones) {
            if (name.equals(bone.getName())) {
                return bone;
            }
        }
        if (notNull) {
            throw new RuntimeException("No such bone: " + name);
        } else {
            return null;
        }
    }

    // ----- Abstract ----- //

    protected BoneEntity createBone(Node node, BoneEntity parentBone, Param p) {
        return new BoneEntity(BoneEntity.Param.builder()
                .ragdoll(this)
                .node(node)
                .parentBone(parentBone)
                .rigidBody(getBoneRigidBody(node, parentBone, p))
                .constraint(getBoneConstraint(node, parentBone, p))
                .build());
    }

    protected Constraint getBoneConstraint(Node node, BoneEntity parentBone, Param p) {
        if (parentBone == null) return null;

        float swingSpanLimit = ModelEntity.getFloat(node, "swing|ls", p.defaultSwingSpanLimit);
        float twistSpanLimit = ModelEntity.getFloat(node, "twist|lt", p.defaultTwistSpanLimit);

        ConeTwistConstraint constraint;
        constraint = new ConeTwistConstraint(parentBone, new Matrix4().set(node.localTransform), new Matrix4());
        constraint.disableCollisionsBetweenLinkedBodies = true;
        constraint.breakingImpulseThreshold = 2000000f;
        constraint.swingSpan1 = swingSpanLimit;
        constraint.swingSpan2 = swingSpanLimit;
        constraint.twistSpan = twistSpanLimit;
        constraint.softness = 0.5f;
        constraint.biasFactor = 0.3f;

        return constraint;
    }

    protected RigidBody getBoneRigidBody(Node node, BoneEntity parentBone, Param p) {
        float mass = ModelEntity.getFloat(node, "mass|m", p.defaultBoneMass);
        float radius = ModelEntity.getFloat(node, "radius|r", p.defaultBoneRadius);
        float height = ModelEntity.getFloat(node, "length|l", p.defaultBoneHeight);
        boolean isKinematic = ModelEntity.getBool(node, "isKinematic|k");

        btCollisionShape shape = getBoneShape(node, radius, height);

        RigidBody rigidBody;
        rigidBody = new TemplateRigidBody(shape, mass);
        rigidBody.mask = ~RAGDOLL_GROUP;
        rigidBody.isKinematic = isKinematic;
        rigidBody.autoConvertToLocalTransform = true;
        rigidBody.collisionFlags = 0;
        rigidBody.body.setDamping(0.2f, 0.2f);

        return rigidBody;
    }

    protected btCollisionShape getBoneShape(Node node, float radius, float height) {
        btCollisionShape shape;

        String id = node.id.replaceAll("^([^\\[]*)\\[.*$", "$1");
        Node bodyNode = modelInstance.getNode(id + ".body");

        if (bodyNode != null) {
            shape = getConvexHullShape(node, bodyNode);
        } else {
            shape = getCapsuleShape(node, radius, height);
        }

        return shape;
    }

    protected btCollisionShape getConvexHullShape(Node node, Node bodyNode) {
        btCollisionShape shape;
        Matrix4 tmpM = Matrix4Pool.obtain();

        tmpM.set(node.calculateWorldTransform()).inv().mul(bodyNode.calculateWorldTransform());
        shape = MeshUtil.getCompoundShape(bodyNode, tmpM);
        bodyNode.getParent().removeChild(bodyNode);

        Matrix4Pool.free(tmpM);
        return shape;
    }

    protected btCollisionShape getCapsuleShape(Node node, float radius, float height) {
        btCollisionShape shape;

        Vector3 tmpV = Vector3Pool.obtain();
        Matrix4 tmpM = Matrix4Pool.obtain();

        btCompoundShape compoundShape = new btCompoundShape();
        compoundShape.addChildShape(
                tmpM.setToTranslation(0, height / 2f, 0),
                new btCapsuleShape(radius, height)
        );
        shape = compoundShape;

        Matrix4Pool.free(tmpM);
        Vector3Pool.free(tmpV);
        return shape;
    }
}
