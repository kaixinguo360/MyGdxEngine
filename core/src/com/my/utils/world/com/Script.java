package com.my.utils.world.com;

import com.my.utils.world.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Script implements Component, StandaloneResource {
    @Config
    public boolean disabled;
    public abstract void init(World world, Entity entity);
    public abstract void execute(World world, Entity entity);
}
