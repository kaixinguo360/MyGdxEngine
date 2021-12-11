package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.ScriptSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class ScriptComponent implements Component {
    public ScriptSystem.Script script;
    public Map<String, Object> config;
    public Object customObj;
}
