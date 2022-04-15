package com.my.world.core;

import lombok.Getter;
import lombok.Setter;

public class TimeManager implements Configurable {

    @Getter @Setter @Config private float timeScale = 1;

    @Getter @Config private float realDeltaTime = 0;

    @Getter @Config private float realCurrentTime = 0;
    @Getter @Config private float deltaTime = 0;

    @Getter @Config private float currentTime = 0;
    @Getter @Config private long frameCount = 0;

    public void update(float realDeltaTime) {

        this.realDeltaTime = realDeltaTime;
        this.realCurrentTime += realDeltaTime;

        this.deltaTime = realDeltaTime * timeScale;
        this.currentTime += deltaTime;

        this.frameCount++;
    }
}
