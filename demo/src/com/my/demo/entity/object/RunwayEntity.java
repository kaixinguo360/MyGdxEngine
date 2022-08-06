package com.my.demo.entity.object;

import com.my.world.enhanced.entity.EnhancedEntity;

public class RunwayEntity extends EnhancedEntity {

    public RunwayEntity() {
        setName("Runway");
        for (int i = 0; i < 100; i++) {
            BoxEntity box1 = new BoxEntity();
            box1.setParent(this);
            box1.transform.translate(10, 0.5f, -10 * i);
            addEntity(box1);

            BoxEntity box2 = new BoxEntity();
            box2.setParent(this);
            box2.transform.translate(-10, 0.5f, -10 * i);
            addEntity(box2);
        }
    }
}
