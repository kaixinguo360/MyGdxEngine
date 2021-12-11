package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Component;
import com.my.utils.world.sys.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Render implements Component {

    public RenderSystem.RenderConfig renderConfig;
    public ModelInstance modelInstance;
    public boolean includeEnv;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

}
