package com.my.utils.world.com;

import com.my.utils.world.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Script implements Component, LoadableResource {

    public boolean disabled;
    public abstract void init(World world, Entity entity);
    public abstract void execute(World world, Entity entity);

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        this.disabled = (Boolean) config.get("disabled");
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        return new HashMap<String, Object>() {{
            put("disabled", disabled);
        }};
    }
}
