package com.my.demo.script;

import com.badlogic.gdx.Input;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Event;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.ArrayList;
import java.util.List;

public class CharacterSwitcher implements ScriptSystem.OnStart, InputSystem.OnKeyDown {

    public static final String DEFAULT_EVENT_ID = "CharacterSwitch";

    @Config public String eventId = DEFAULT_EVENT_ID;

    @Config public int keySwitch = Input.Keys.TAB;

    @Config public int currentIndex = 0;

    @Config(elementType = String.class)
    public final List<String> characterNames = new ArrayList<>();

    private Scene scene;

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == keySwitch) {
            currentIndex = (currentIndex + 1) % characterNames.size();
            scene.getEventManager().dispatchEvent(eventId, new Event(this, characterNames.get(currentIndex)));
        }
    }
}
