package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.my.world.enhanced.entity.EnhancedEntity;

public class ChoppingEntity extends EnhancedEntity {

    public final EnhancedChoppingScript choppingScript;
    public final DetectorEntity detector;
    public final CutterEntity cutter;

    public ChoppingEntity(Model cutterModel) {
        setName("ChoppingEntity");

        choppingScript = addComponent(new EnhancedChoppingScript());

        detector = DetectorEntity.sphere(10);
        detector.setParent(this);
        addEntity(detector);

        cutter = CutterEntity.get(cutterModel);
        cutter.setParent(this);
        addEntity(cutter);
    }
}
