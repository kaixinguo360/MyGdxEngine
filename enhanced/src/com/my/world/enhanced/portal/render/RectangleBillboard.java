package com.my.world.enhanced.portal.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.render.model.ProceduralModelRender;

public class RectangleBillboard extends ProceduralModelRender {

    @Config public float width;
    @Config public float height;

    public RectangleBillboard(float width, float height, Material material, long attributes) {
        this.width = width;
        this.height = height;
        this.material = material;
        this.attributes = attributes;
        init();
    }

    @Override
    public void init() {
        // 00 01
        // 10 11
        float x = width / 2, y = height / 2;
        float x00 = -x, y00 = y, z00 = 0;
        float x10 = -x, y10 = -y, z10 = 0;
        float x11 = x, y11 = -y, z11 = 0;
        float x01 = x, y01 = y, z01 = 0;
        float normalX = 0, normalY = 0, normalZ = 1;

        mdBuilder.begin();
        MeshPartBuilder part = mdBuilder.part("rectangle", GL20.GL_TRIANGLES, attributes, material);
        part.rect(
                x00, y00, z00,
                x10, y10, z10,
                x11, y11, z11,
                x01, y01, z01,
                normalX, normalY, normalZ
        );
        part.rect(
                x00, y00, z00,
                x01, y01, z01,
                x11, y11, z11,
                x10, y10, z10,
                normalX, normalY, -normalZ
        );
        model = mdBuilder.end();

        super.init();
    }

    @Override
    public boolean isVisible(Camera cam) {
        Vector3 tmpV1 = Vector3Pool.obtain();
        Vector3 tmpV2 = Vector3Pool.obtain();
        this.modelInstance.transform.getTranslation(tmpV1);
        this.modelInstance.transform.idt();
        this.modelInstance.transform.setToLookAt(tmpV2.set(cam.position).sub(tmpV1), Vector3.Y).inv();
        this.modelInstance.transform.trn(tmpV1);
        Vector3Pool.free(tmpV2);
        Vector3Pool.free(tmpV1);
        return super.isVisible(cam);
    }
}
