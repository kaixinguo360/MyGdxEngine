package com.my.utils.world.com;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Position implements Component {
    @Config public Matrix4 transform;
}
