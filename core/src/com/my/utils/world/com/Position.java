package com.my.utils.world.com;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.Component;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Position implements Component {
    private Matrix4 transform;
}
