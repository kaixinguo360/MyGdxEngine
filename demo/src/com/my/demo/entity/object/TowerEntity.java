package com.my.demo.entity.object;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.EnhancedEntity;

public class TowerEntity extends EnhancedEntity {

    public final WallEntity wall1;
    public final WallEntity wall2;
    public final WallEntity wall3;
    public final WallEntity wall4;

    public TowerEntity() {
        setName("Tower");

        wall1 = new WallEntity();
        wall1.setParent(this);
        wall1.setName("Wall-1");
        addEntity(wall1);

        wall2 = new WallEntity();
        wall2.setParent(this);
        wall2.setName("Wall-2");
        wall2.transform.translate(0, 0, 10).rotate(Vector3.Y, 90);
        addEntity(wall2);

        wall3 = new WallEntity();
        wall3.setParent(this);
        wall3.setName("Wall-3");
        wall3.transform.translate(10, 0, 10).rotate(Vector3.Y, 180);
        addEntity(wall3);

        wall4 = new WallEntity();
        wall4.setParent(this);
        wall4.setName("Wall-4");
        wall4.transform.translate(10, 0, 0).rotate(Vector3.Y, 270);
        addEntity(wall4);
    }
}
