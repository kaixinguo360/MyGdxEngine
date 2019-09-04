package com.my.game;

import com.badlogic.gdx.graphics.PerspectiveCamera;

public interface Controllable {
    void update();
    void setCamera(PerspectiveCamera camera, int index);
    void fire();
    void explode();
}
