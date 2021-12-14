package com.my.utils.world.com;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.Component;
import com.my.utils.world.LoadContext;
import com.my.utils.world.LoadableResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Position implements Component, LoadableResource {

    public Matrix4 transform;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        float[] values = new float[16];
        for (int i = 0; i < values.length; i++) {
            values[i] = (float) (double) config.get("v" + i);
        }
        this.transform = new Matrix4(values);
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        float[] values = this.transform.getValues();
        return new HashMap<String, Object>() {{
            for (int i = 0; i < values.length; i++) {
                put("v" + i, (double) values[i]);
            }
        }};
    }
}
