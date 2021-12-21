package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Collision implements Component {

    @Config public int callbackFlag;
    @Config public int callbackFilter;

}
