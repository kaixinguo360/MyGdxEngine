package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.my.world.core.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Light implements Component {

    public BaseLight light;
}
