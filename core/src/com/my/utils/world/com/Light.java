package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.my.utils.world.Component;
import com.my.utils.world.LoadContext;
import com.my.utils.world.LoadableResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Light implements Component, LoadableResource {

    public BaseLight light;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        return null;
    }

    private enum LightType {
        Directional,
        Point,
        Spot;
    }
}
