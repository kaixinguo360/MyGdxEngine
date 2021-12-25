package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Collision implements Component {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config public int callbackFlag;
    @Config public int callbackFilter;

}
