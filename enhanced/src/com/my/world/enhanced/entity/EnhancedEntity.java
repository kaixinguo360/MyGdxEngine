package com.my.world.enhanced.entity;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.module.common.EnhancedPosition;

import java.util.ArrayList;
import java.util.List;

public class EnhancedEntity extends Entity {

    public final EnhancedPosition position;
    public final Matrix4 transform;
    public final Vector3 translation;
    public final Vector3 rotation;
    public final Vector3 scale;

    public List<EnhancedEntity> children = new ArrayList<>();

    public EnhancedEntity(Param p) {
        this();
        setName(p.name);
    }

    public EnhancedEntity() {
        transform = new Matrix4();
        position = addComponent(new EnhancedPosition(transform));
        translation = position.translation;
        rotation = position.rotation;
        scale = position.scale;
    }

    public void decompose() {
        position.decompose();
    }

    public void compose() {
        position.compose();
    }

    protected  <T extends EnhancedEntity> T addEntity(T child) {
        children.add(child);
        return child;
    }

    public void addToScene(Scene scene) {
        scene.addEntity(this);
        for (EnhancedEntity child : children) {
            child.addToScene(scene);
        }
    }

    public abstract static class Param implements EntityGenerator {

        @Config public String name;

        @Override
        public EnhancedEntity generate() {
            return build();
        }

        public abstract EnhancedEntity build();
    }

}
