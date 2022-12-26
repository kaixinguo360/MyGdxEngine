package com.my.world.enhanced.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.module.physics.script.ConstraintController;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HingeConstraintController extends ConstraintController {

    @Config public float low = 0;
    @Config public float high = 0;
    @Config public boolean limit = false;
    @Config public float resilience = 0;
    @Config public float current = 0;

    protected boolean isChanged = false;

    public HingeConstraintController(float low, float high) {
        this(low, high, 0);
    }

    public HingeConstraintController(float low, float high, float resilience) {
        this.limit = true;
        this.low = low;
        this.high = high;
        this.resilience = resilience;
    }

    public void rotate(float step) {
        isChanged = true;
        current += step;
    }

    @Override
    public void update(btTypedConstraint constraint) {
        if (!isChanged && resilience > 0) {
            if (Math.abs(current) < resilience) {
                current = 0;
            } else {
                current += current > 0 ? -resilience : (current < 0 ? resilience : 0);
            }
        }
        if (isChanged) activate();
        isChanged = false;
        if (limit) {
            current = Math.min(high, current);
            current = Math.max(low, current);
        }
        ((btHingeConstraint) constraint).setLimit(current, current, 0, 0.5f);
    }
}
