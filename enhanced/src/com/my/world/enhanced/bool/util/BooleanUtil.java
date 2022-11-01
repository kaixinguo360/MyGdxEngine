package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.my.world.core.Entity;
import com.my.world.enhanced.bool.operation.BooleanOperationException;
import com.my.world.enhanced.bool.operation.ModelInstanceBoolOperation;
import com.my.world.module.common.Position;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.BaseRender;
import com.my.world.module.render.Render;

import java.util.ArrayList;
import java.util.List;

public class BooleanUtil {

    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();

    public static void cut(ModelInstance targetModelInstance, Model cutter, Matrix4 transform, Type type) throws BooleanOperationException {
        if (targetModelInstance == null || cutter == null) return;

        Node node = cutter.nodes.first();
        MeshPart reference = node.parts.first().meshPart;

        ModelInstanceBoolOperation boolOperation = new ModelInstanceBoolOperation(targetModelInstance, reference, transform);
        switch (type) {
            case UNION:
                boolOperation.doUnion();
                break;
            case DIFF:
                boolOperation.doDifference();
                break;
            case INTER:
                boolOperation.doIntersection();
                break;
        }
        boolOperation.apply();
    }

    public static List<Entity> cut(Entity targetEntity, Model cutter, Matrix4 transform, Type type) throws BooleanOperationException {
        if (targetEntity == null || cutter == null) return null;

        Node node = cutter.nodes.first();
        MeshPart reference = node.parts.first().meshPart;
        ModelInstance instance = targetEntity.getComponent(BaseRender.class).modelInstance.copy();
        List<Entity> newEntities = new ArrayList<>();
        Matrix4 entityTransform = new Matrix4(targetEntity.getComponent(Position.class).getGlobalTransform());

        // 创建BoolOperation
        ModelInstanceBoolOperation bool;
        try {
            bool = new ModelInstanceBoolOperation(instance, reference, transform.cpy().mul(node.localTransform));
        } catch (BooleanOperationException e) {
            LoggerUtil.log(3, "Error Occurs In Boolean Cut Operation!");
            return null;
        }

        if (bool.skip) {
            // 无相交的meshPart, 或出错, 直接返回
            LoggerUtil.log(0, "无相交的meshPart, 或出错, 直接返回!");
            return null;
        } else {
            LoggerUtil.log(0, "有相交的meshPart");
        }

        // 获取相交的部分
        if (type == Type.UNION || type == Type.INTER) {
            bool.doIntersection();
            ModelInstance intersectionInstance = bool.getNewModelInstance();
            if (MeshUtil.hasMesh(intersectionInstance)) {
                List<ModelInstance> instances = MeshSplitter.splitModeInstances(intersectionInstance);
                for (ModelInstance newInstance : instances) {
                    System.out.println("相交的部分: " + instances.size());
                    newEntities.add(toEntity(targetEntity, entityTransform, newInstance, 10f));
                }
            }
        }

        // 获取不相交的部分
        if (type == Type.UNION || type == Type.DIFF) {
            bool.doDifference();
            ModelInstance differenceInstance = bool.getNewModelInstance();
            if (MeshUtil.hasMesh(differenceInstance)) {
                List<ModelInstance> instances = MeshSplitter.splitModeInstances(differenceInstance);
                System.out.println("不相交的部分: " + instances.size());
                for (ModelInstance newInstance : instances) {
                    newEntities.add(toEntity(targetEntity, entityTransform, newInstance, 10f));
                }
            }
        }

        return newEntities;
    }

    public static Entity toEntity(Entity original, Matrix4 transform, ModelInstance instance, float mass) {
        RigidBody originalRigidBody = original.getComponent(RigidBody.class);
        Entity newEntity = new Entity();
        newEntity.setName(original.getName());

        Position position = new Position(new Matrix4());

        instance.calculateBoundingBox(bounds);
        tmpV.set(bounds.getCenterX(), bounds.getCenterY(), bounds.getCenterZ());
        tmpM.setToTranslation(tmpV);

        instance.transform.mul(tmpM);
        position.getLocalTransform().set(transform).mul(tmpM);

        tmpM.inv();
        instance.nodes.first().translation.mul(tmpM);
        instance.calculateTransforms();

        Render render = new BaseRender(instance);
        render.includeEnv = true;

        btConvexHullShape shape = BulletUtil.getConvexHullShape(instance.nodes);
        RigidBody rigidBody = new TemplateRigidBody(shape, mass);
        rigidBody.group = originalRigidBody.group;
        rigidBody.mask = originalRigidBody.mask;
        rigidBody.body.setLinearVelocity(originalRigidBody.body.getLinearVelocity());
        rigidBody.body.setAngularVelocity(originalRigidBody.body.getAngularVelocity());

        newEntity.addComponent(position);
        newEntity.addComponent(render);
        newEntity.addComponent(rigidBody);

        return newEntity;
    }

    public enum Type {
        DIFF, INTER, UNION
    }
}
