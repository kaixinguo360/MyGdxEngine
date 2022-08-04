package com.my.world.enhanced.ragdoll;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.BaseRender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Iterator;

import static com.my.world.enhanced.ragdoll.MeshUtil.getCompoundShape;
import static com.my.world.enhanced.ragdoll.MeshUtil.getConvexHullShape;

public class ModelEntity extends EnhancedEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Param {
        public Model model;
        public String rootNodeId;
        @Builder.Default public boolean parentTransform = false;
        @Builder.Default public boolean mergeTransform = true;
        @Builder.Default public float defaultObjectMass = 50f;
    }

    public final BaseRender render;
    public final TemplateRigidBody rigidBody;

    public ModelEntity(Param p) {

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
        if (node.parts.size != 0) {
            // Add Render
            ModelInstance modelInstance = new ModelInstance(p.model, node.id, true, false, true);
            Node rootNode = modelInstance.getNode(node.id);
            Iterator<Node> it = rootNode.getChildren().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
            render = addComponent(new BaseRender(modelInstance));
            // Add RigidBody
            btCollisionShape shape;
            Node bodyNode = node.getChild(id + ".body", true, false);
            if (bodyNode != null) {
                Matrix4 tmpM = Matrix4Pool.obtain();

                tmpM.set(node.calculateWorldTransform()).inv().mul(bodyNode.calculateWorldTransform());
                shape = getCompoundShape(bodyNode, tmpM);

                Matrix4Pool.free(tmpM);
            } else {
                shape = getConvexHullShape(node, null);
            }
            rigidBody = addComponent(new TemplateRigidBody(
                    shape,
                    getFloat(node, "mass|m", p.defaultObjectMass)
            ));
            rigidBody.isKinematic = getBool(node, "isKinematic|k");
            rigidBody.autoConvertToLocalTransform = true;
            rigidBody.body.setDamping(0.2f, 0.2f);
        } else {
            render = null;
            rigidBody = null;
        }

        // Children Entity
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChild(i);
            if (child.id.endsWith(".body")) continue;
            EnhancedEntity entity;
            if (getBool(child, "armature|a")) {
                RagdollEntity.Param param = RagdollEntity.Param.builder()
                        .defaultBoneRadius(getFloat(child, "radius|r", 0.125f))
                        .defaultBoneHeight(getFloat(child, "length|l", 0.5f))
                        .defaultSwingSpanLimit(getFloat(child, "swing|ls", 60f))
                        .defaultTwistSpanLimit(getFloat(child, "twist|lt", 0))
                        .defaultBoneMass(getFloat(child, "boneMass|bm", 100f))
                        .mass(getFloat(child, "mass|m", 1000f))
                        .model(p.model)
                        .rootNodeId(child.id)
                        .rootBodyId(getString(child, "rootBody|a.r", "MainBody"))
                        .rootBoneId(getString(child, "rootBody|a.b", "MainBone"))
                        .isKinematic(getBool(child, "isKinematic|k"))
                        .build();
                entity = new RagdollEntity(param);
            } else {
                entity = new ModelEntity(new Param() {{
                    model = p.model;
                    rootNodeId = child.id;
                    defaultObjectMass = p.defaultObjectMass;
                }});
            }
            entity.setParent(this);
            addEntity(entity);
        }
    }

    // ----- Static ----- //

    public static float getFloat(Node node, String key, float defaultValue) {
        String result = node.id.replaceAll("^.*\\[(" + key + ")=([0-9.f]*)].*$", "$2");
        if (result.equals(node.id)) {
            return defaultValue;
        } else {
            return Float.parseFloat(result);
        }
    }

    public static String getString(Node node, String key, String defaultValue) {
        String result = node.id.replaceAll("^.*\\[(" + key + ")=([^\\[]*)].*$", "$2");
        if (result.equals(node.id)) {
            return defaultValue;
        } else {
            return result.replace("(", "[").replace(")", "]");
        }
    }

    public static boolean getBool(Node node, String key) {
        return node.id.matches("^.*\\[(" + key + ")].*$");
    }
}
