package com.my.utils.world.com;

import com.my.utils.world.Component;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Collision implements Component {
    private int callbackFlag;
    private int callbackFilter;
    private final String handler;
}
