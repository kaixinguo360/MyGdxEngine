package com.my.demo.builder.aircraft;

import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.module.physics.script.ConstraintController;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AircraftController extends ConstraintController {

    @Config
    public float low;
    @Config
    public float high;
    @Config
    public float resilience;
    @Config
    public float target = 0;
    @Config
    public boolean isRotated = false;

    public AircraftController(float low, float high, float resilience) {
        this.low = low;
        this.high = high;
        this.resilience = resilience;
    }

    public void rotate(float step) {
        isRotated = true;
        target += step;
    }

    @Override
    public void update(btTypedConstraint constraint) {
        if (!isRotated) {
            target += target > 0 ? -resilience : (target < 0 ? resilience : 0);
        }
        isRotated = false;
        target = Math.min(high, target);
        target = Math.max(low, target);
        btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
        hingeConstraint.setLimit(target, target, 0, 0.5f);
    }
}
