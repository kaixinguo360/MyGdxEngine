package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.sys.CameraSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Camera implements Component {

    @Config public float startX;
    @Config public float startY;

    @Config public float endX;
    @Config public float endY;

    @Config public int layer;
    @Config public CameraSystem.FollowType followType;
}
