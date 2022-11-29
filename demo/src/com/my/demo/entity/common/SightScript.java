package com.my.demo.entity.common;

import com.badlogic.gdx.graphics.Color;
import com.my.world.core.Config;
import com.my.world.enhanced.util.SimpleGuiDrawer;
import com.my.world.module.camera.CameraSystem;

public class SightScript implements CameraSystem.AfterAllRender {

    @Config public final Color squareColor = new Color(1, 1, 1, 0.2f);
    @Config public final Color lineColor = new Color(0, 0, 0, 0.2f);
    @Config public float squareSize = 100;
    @Config public float lineLength = 200;
    @Config public float lineWeight = 2;

    protected final SimpleGuiDrawer drawer;

    public SightScript() {
        drawer = new SimpleGuiDrawer();
        drawer.origin.set(0.5f, 0.5f);
    }

    @Override
    public void afterAllRender() {
        drawer.begin();
        // Draw Square
        drawer.setColor(squareColor);
        drawer.square(0, 0, squareSize);
        // Draw Cross
        drawer.setColor(lineColor);
        drawer.rectLine(-lineLength / 2, 0, lineLength / 2, 0, lineWeight);
        drawer.rectLine(0, -lineLength / 2, 0, lineLength / 2, lineWeight);
        drawer.end();
    }
}
