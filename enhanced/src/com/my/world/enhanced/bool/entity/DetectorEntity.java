package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.SphereBody;

public class DetectorEntity extends EnhancedEntity {

    public final RigidBody rigidBody;
    public final DetectorScript detectorScript;

    public DetectorEntity(RigidBody rigidBody) {
        setName("Detector");
        this.rigidBody = addComponent(rigidBody);
        this.rigidBody.isTrigger = true;
        this.rigidBody.isKinematic = true;
        this.rigidBody.isEnableCallback = true;
        this.detectorScript = addComponent(new DetectorScript());
    }

    public static DetectorEntity sphere(float radius) {
        return new DetectorEntity(new SphereBody(radius, 0, true));
    }

    public static DetectorEntity box(Vector3 boxHalfExtents) {
        return new DetectorEntity(new BoxBody(boxHalfExtents, 0, true));
    }
}
