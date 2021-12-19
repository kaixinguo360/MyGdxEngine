package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.StandaloneResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Script implements Component, StandaloneResource {

    @Config
    public boolean disabled;

}
