package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.MotionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Motion implements Component {
    public MotionSystem.MotionHandler handler;
    public Map<String, Object> config;
}
