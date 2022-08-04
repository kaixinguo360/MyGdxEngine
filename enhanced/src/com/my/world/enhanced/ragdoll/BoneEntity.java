package com.my.world.enhanced.ragdoll;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.Constraint;
import com.my.world.module.physics.RigidBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

public class BoneEntity extends EnhancedEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Param {
        public RagdollEntity ragdoll;
        public Node node;
        public BoneEntity parentBone;
        public Constraint constraint;
        public RigidBody rigidBody;
    }

    public final RagdollEntity ragdoll;
    public final Node node;
    public final BoneEntity parentBone;

    public final RigidBody rigidBody;
    public final Constraint constraint;

    public BoneEntity(Param p) {

        ragdoll = p.ragdoll;
        node = p.node;
        parentBone = p.parentBone;

        // Current Node Entity
        String id = node.id.replaceAll("^([^\\[]*)\\[.*$", "$1");
        setName(id);
        // Set Position
        position.setLocalTransform(node.calculateLocalTransform());
        // Add RigidBody
        rigidBody = addComponent(p.rigidBody);
        // Add Constraint
        constraint = addComponent(p.constraint);
    }

    public void calculateBoneGlobalTransform() {
        ragdoll.position.getGlobalTransform(node.globalTransform).inv().mul(position.getGlobalTransform());
    }

}
