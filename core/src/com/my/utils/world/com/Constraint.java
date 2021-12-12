package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.ConstraintSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Constraint implements Component {
    public String bodyA;
    public String bodyB;
    public ConstraintSystem.ConstraintType type;
    public Map<String, Object> config;
    public ConstraintSystem.ConstraintController controller;
}
