package com.my.world.module.physics.rigidbody;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.ModelRender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ModelStaticBody extends TemplateRigidBody implements Loadable.OnInit {

    @Config(type = Config.Type.Asset)
    public ModelRender modelRender;

    public ModelStaticBody(ModelRender modelRender) {
        this(modelRender, false);
    }

    public ModelStaticBody(ModelRender modelRender, boolean isTrigger) {
        super(isTrigger);
        this.modelRender = modelRender;
        this.mass = 0;
        this.group = STATIC_FLAG;
        init();
    }

    @Override
    public void init() {
        shape = Bullet.obtainStaticNodeShape(modelRender.model.nodes);
        super.init();
    }
}
