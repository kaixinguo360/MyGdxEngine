package com.my.world.enhanced.shader.depthmask.entity;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.my.world.core.Config;
import com.my.world.module.render.model.ProceduralModelRender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HollowCylinder extends ProceduralModelRender {

    @Config private float radius;
    @Config private float height;
    @Config private int divisions;

    public HollowCylinder(float radius, float height, int divisions, Material material, long attributes) {
        this.radius = radius;
        this.height = height;
        this.divisions = divisions;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {

        mdBuilder.begin();
        MeshPartBuilder part = mdBuilder.part("cylinder", GL20.GL_TRIANGLES, attributes, material);
        HollowCylinderShapeBuilder.build(part, radius * 2, height, radius * 2, divisions);
        model = mdBuilder.end();

        super.init();
    }
}
