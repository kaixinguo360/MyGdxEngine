package com.my.utils.world.com;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.StandaloneResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Position implements Component, StandaloneResource {
    @Config public Matrix4 transform;
}
