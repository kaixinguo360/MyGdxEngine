package com.my.demo.entity.common;

import com.my.world.core.*;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.PerspectiveCamera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.Script;
import com.my.world.module.script.ScriptSystem;

public class CharacterSwitcherAgent implements ScriptSystem.OnStart, ScriptSystem.OnRemoved {

    @Config public String eventId = CharacterSwitcher.DEFAULT_EVENT_ID;

    @Config public String cameraName = "camera";

    @Config public String characterName;

    protected Entity entity;

    @Override
    public void start(Scene scene, Entity entity) {
        this.entity = entity;
        scene.getEventManager().addEventListener(eventId, eventListener);
    }

    @Override
    public void removed(Scene scene, Entity entity) {
        scene.getEventManager().removeEventListener(eventId, eventListener);
    }

    protected EventListener eventListener = (Event event) -> {
        String currentCharacterName = event.getData(String.class);
        boolean active = this.characterName.equals(currentCharacterName);

        for (Script script : entity.getComponents(Script.class)) {
            if (script instanceof Activatable) {
                ((Activatable) script).setActive(active);
            }
        }

        Entity cameraEntity = entity.findChildByName(cameraName);
        if (cameraEntity != null) {
            Camera camera = cameraEntity.getComponent(PerspectiveCamera.class);
            EnhancedThirdPersonCameraController cameraController = cameraEntity.getComponent(EnhancedThirdPersonCameraController.class);
            if (camera != null) camera.setActive(active);
            if (cameraController != null) cameraController.setActive(active);
        }
    };
}
