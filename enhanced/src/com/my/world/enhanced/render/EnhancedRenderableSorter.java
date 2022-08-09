package com.my.world.enhanced.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;

public class EnhancedRenderableSorter extends SceneRenderableSorter {

    @Override
    public int compare(Renderable o1, Renderable o2) {
        int b1 = o1.material.has(RenderOrderAttribute.RenderOrder)
                ? o1.material.get(RenderOrderAttribute.class, RenderOrderAttribute.RenderOrder).order : 0;
        int b2 = o2.material.has(RenderOrderAttribute.RenderOrder)
                ? o2.material.get(RenderOrderAttribute.class, RenderOrderAttribute.RenderOrder).order : 0;
        if (b1 > b2) {
            return 1;
        } else if (b1 < b2) {
            return -1;
        } else {
            return super.compare(o1, o2);
        }
    }
}
