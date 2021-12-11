package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Collision implements Component {
    public int callbackFlag;
    public int callbackFilter;
    public PhysicsSystem.CollisionHandler handler;
}
