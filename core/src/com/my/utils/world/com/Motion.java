package com.my.utils.world.com;

import com.my.utils.world.Component;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Motion implements Component {
    private String type;
    private Map<String, Object> config;
}
