package com.my.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.my.utils.BulletUtils;
import com.my.utils.MeshSplitter;
import com.my.utils.MeshUtils;
import com.my.utils.MyLogger;
import com.my.utils.bool.BooleanOperationException;
import com.my.utils.bool.ModelInstanceBoolOperation;
import com.my.utils.world.mod.ModelComponent;
import com.my.utils.world.mod.PhyComponent;

public class BooleanInstance extends MyInstance {

    public BooleanInstance(String name) {
        super(name);
    }
    public BooleanInstance(ModelComponent modelComponent, PhyComponent phyComponent) {
        super(modelComponent, phyComponent);
    }

    public Array<BooleanInstance> cut(Model cutter, Matrix4 transform) throws BooleanOperationException {

        Node node = cutter.nodes.first();
        MeshPart reference = node.parts.first().meshPart;
        ModelInstance instance = this.modelComponent.getModelInstance();
        Array<BooleanInstance> newInstances = new Array<>();

        try {
            //创建BoolOperation
            ModelInstanceBoolOperation bool;
            bool = new ModelInstanceBoolOperation(instance, reference, transform.cpy().mul(node.localTransform));

            //创建新Instance
            bool.doIntersection();
            ModelInstance newModelInstance = bool.getNewModelInstance();
            if(MeshUtils.hasMesh(newModelInstance)) {
                Array<ModelInstance> instances = MeshSplitter.splitModeInstances(newModelInstance);
                for(ModelInstance news : instances) {
                    newInstances.add(getNewInstance(news, 10f));
                }
            }
//            newInstances.add(getNewInstance(newModelInstance, 10f));

            //更新旧Instance
            bool.doDifference();
            bool.apply();

            float max = 0;
            ModelInstance maxInstance = null;
            if(MeshUtils.hasMesh(instance)) {
                Array<ModelInstance> instances = MeshSplitter.splitModeInstances(instance);
                for(ModelInstance news : instances) {
                    news.calculateBoundingBox(bounds);
                    bounds.getDimensions(tmpV);
                    if (tmpV.len()>max) {
                        max = tmpV.len();
                        maxInstance = news;
                    }
                }
                for(ModelInstance news : instances) {
                    newInstances.add(getNewInstance(news, (maxInstance == news) ? 0 : 10f));
                }
            }
//            newInstances.add(getNewInstance(instance, 0));
            return newInstances;
        } catch (BooleanOperationException e) {
            MyLogger.log(3, "Error Occurs In Boolean Operation!");
            throw e;
        }
    }
    public BooleanInstance getNewInstance(ModelInstance instance, float mass) {
        if (mass != 0) {
            instance.calculateBoundingBox(bounds);
            tmpV.set(bounds.getCenterX(), bounds.getCenterY(), bounds.getCenterZ());
            tmpM.setToTranslation(tmpV);
            instance.transform.mul(tmpM);
            tmpM.inv();
            instance.nodes.first().translation.mul(tmpM);
            instance.calculateTransforms();
        }

        ModelComponent modelComponent = ModelComponent.pool.obtain().set(instance, true);
        PhyComponent.Config phyConfig = new PhyComponent.Config(BulletUtils.getConvexHullShape(instance), mass);
        PhyComponent phyComponent = PhyComponent.pool.obtain().set(phyConfig);

        BooleanInstance newInstance = new BooleanInstance(modelComponent, phyComponent);
        newInstance.setTransform(this.modelComponent.getTransform());
        return newInstance;
    }


    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
}
