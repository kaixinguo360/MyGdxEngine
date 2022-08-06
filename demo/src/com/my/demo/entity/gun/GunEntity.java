package com.my.demo.entity.gun;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.object.RotateEntity;
import com.my.demo.entity.weapon.BombEntity;
import com.my.demo.entity.weapon.BulletEntity;
import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.common.Position;

public class GunEntity extends EnhancedEntity {

    public final GunScript gunScript;
    public final RotateEntity<GunController> rotateY;
    public final RotateEntity<GunController> rotateX;
    public final BarrelEntity barrel;

    public GunEntity() {
        this(null);
    }

    public GunEntity(Entity baseEntity) {
        setName("Gun");
        gunScript = addComponent(new GunScript());
        gunScript.bulletBuilder = BulletEntity.builder;
        gunScript.bombBuilder = BombEntity.builder;

        Matrix4 transform = new Matrix4().translate(0, 1.5f, 0).rotate(Vector3.Z, 90);

        rotateY = new RotateEntity<>(baseEntity, new GunController());
        rotateY.setName("rotate_Y");
        rotateY.setParent(this);
        rotateY.transform.translate(0, 0.5f, 0);
        rotateY.decompose();
        addEntity(rotateY);

        rotateX = new RotateEntity<>(rotateY, new GunController(-90,  0));
        rotateX.setName("rotate_X");
        rotateX.setParent(this);
        rotateX.transform.set(transform);
        rotateX.decompose();
        rotateX.constraint.frameInA.set(rotateY.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform).rotate(Vector3.X, 90));
        addEntity(rotateX);

        barrel = new BarrelEntity(rotateX);
        barrel.setName("barrel");
        barrel.setParent(this);
        barrel.transform.translate(0, 1.5f, -3);
        barrel.decompose();
        addEntity(barrel);
    }
}
