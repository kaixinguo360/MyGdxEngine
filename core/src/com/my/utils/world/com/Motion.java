package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.sys.MotionSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Motion extends MotionInner implements Component {
    private String type;
    private Map<String, Object> config;
}

@Data
class MotionInner implements Component {
    private MotionSystem.MotionHandler handler;
}
