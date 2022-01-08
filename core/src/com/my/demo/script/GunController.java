package com.my.demo.script;

import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.module.physics.script.ConstraintController;

public class GunController extends ConstraintController {

    @Config
    public float target = 0;
    @Config
    public float max = 0;
    @Config
    public float min = 0;
    @Config
    public boolean limit = false;

    public GunController() {
    }

    public GunController(float min, float max) {
        limit = true;
        this.min = (float) Math.toRadians(min);
        this.max = (float) Math.toRadians(max);
    }

    @Override
    public void update(btTypedConstraint constraint) {
        if (limit) {
            target = Math.min(max, target);
            target = Math.max(min, target);
        }
        btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
        hingeConstraint.setLimit(target, target, 0, 0.5f);
    }
}
