package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Collision extends CollisionInner implements Component {
    private int callbackFlag;
    private int callbackFilter;
    private final String handlerName;
}

@Data
class CollisionInner implements Component {
    private PhysicsSystem.CollisionHandler handler;
}
