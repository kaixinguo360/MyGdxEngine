package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.BaseInstance;
import com.my.utils.world.mod.ModelComponent;
import com.my.utils.world.mod.PhyComponent;

public class MyInstance extends BaseInstance implements Disposable {

    protected ModelComponent modelComponent;
    protected PhyComponent phyComponent;

    public MyInstance() {}

    public MyInstance(String name) {
        modelComponent = ModelComponent.obtainComponent(name);
        if (modelComponent != null) {
            addComponent("model", modelComponent);
        }
        phyComponent = PhyComponent.obtainComponent(name);
        if (phyComponent != null) {
            addComponent("phy", phyComponent);
            phyComponent.setMotionState(modelComponent.getTransform());
        }
    }

    public void setTransform(Matrix4 transform) {
        if (modelComponent != null) {
            modelComponent.getTransform().set(transform);
        }
        if (phyComponent != null) {
            phyComponent.proceedToTransform(transform);
        }
    }

    @Override
    public void dispose() {
        if (modelComponent != null) ModelComponent.pool.free(modelComponent);
        if (phyComponent != null) PhyComponent.pool.free(phyComponent);
    }
}
