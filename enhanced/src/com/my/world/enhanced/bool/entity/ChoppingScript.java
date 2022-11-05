package com.my.world.enhanced.bool.entity;

import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.TimeManager;
import com.my.world.enhanced.util.Retarder;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.ArrayList;
import java.util.List;

public class ChoppingScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, InputSystem.OnTouchDown, InputSystem.OnTouchUp {

    @Config public String detectorName = "Detector";
    @Config public String cutterName = "Cutter";
    @Config public int choppingButton;
    @Config public float normalModeTimeScale = 1;
    @Config public float choppingModeTimeScale = 0.05f;

    protected boolean isChopping = false;
    protected List<Entity> choppingEntities = new ArrayList<>();
    protected TimeManager timeManager;
    protected DetectorEntity detector;
    protected CutterEntity cutter;
    protected Retarder retarder;

    @Override
    public void start(Scene scene, Entity entity) {
        timeManager = scene.getTimeManager();
        detector = (DetectorEntity) entity.findChildByName(detectorName);
        if (detector == null) {
            throw new RuntimeException("No such child entity: name=" + detectorName);
        }
        cutter = (CutterEntity) entity.findChildByName(cutterName);
        if (cutter == null) {
            throw new RuntimeException("No such child entity: name=" + cutterName);
        }
        retarder = new Retarder(timeManager::getRealDeltaTime, timeManager::getTimeScale, timeManager::setTimeScale);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        retarder.update();
    }

    @Override
    public void touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != choppingButton || isChopping) return;
//        if (detector.detectorScript.isEmpty()) return;
        isChopping = true;
        retarder.setValue(choppingModeTimeScale, 0.2f);

        choppingEntities.clear();
        detector.detectorScript.getEntities(choppingEntities);
        System.out.println("Chopping:" + choppingEntities.size());
    }

    @Override
    public void touchUp(int screenX, int screenY, int pointer, int button) {
        if (button != choppingButton || !isChopping) return;
        isChopping = false;
        retarder.setValue(normalModeTimeScale, 2f);

        cutter.cutterScript.doCut(choppingEntities);
        choppingEntities.clear();
    }
}
