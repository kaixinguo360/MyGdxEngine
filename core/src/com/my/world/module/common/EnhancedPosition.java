package com.my.world.module.common;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EnhancedPosition extends Position implements SyncComponent {

    @Config
    public boolean isDirect = false;

    @Config
    public final Vector3 translation = new Vector3();

    @Config
    public final Vector3 rotation = new Vector3();

    @Config
    private final Quaternion orientation = new Quaternion();

    @Config
    public final Vector3 scale = new Vector3();

    public EnhancedPosition(Matrix4 localTransform) {
        super(localTransform);
        decompose();
    }

    @Override
    public void setLocalTransform(Matrix4 transform) {
        super.setLocalTransform(transform);
        decompose();
    }

    @Override
    public void sync() {
        compose();
    }

    public void decompose() {
        localTransform.getTranslation(translation);
        localTransform.getScale(scale);
        localTransform.getRotation(orientation);
        if (!isDirect) {
            rotation.set(orientation.getYaw(), orientation.getPitch(), orientation.getRoll());
        }
    }

    public void compose() {
        if (!isDirect) {
            orientation.setEulerAngles(rotation.x, rotation.y, rotation.z);
        }
        localTransform.set(translation, orientation, scale);
    }
}
