package com.my.demo.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.my.demo.MyWorld;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width * 2 / 3;
        config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height * 2 / 3;
        config.depth = 32;
        config.forceExit = true;
        new LwjglApplication(new MyWorld(), config);
    }
}
