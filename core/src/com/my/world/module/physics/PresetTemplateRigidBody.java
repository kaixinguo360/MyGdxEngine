package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PresetTemplateRigidBody extends RigidBody implements Loadable.OnInit {

    @Config(type = Config.Type.Asset)
    public TemplateRigidBody templateRigidBody;

    public PresetTemplateRigidBody(TemplateRigidBody templateRigidBody) {
        this(templateRigidBody, false);
    }

    public PresetTemplateRigidBody(TemplateRigidBody templateRigidBody, boolean isTrigger) {
        super(isTrigger);
        this.templateRigidBody = templateRigidBody;
        init();
    }

    @Override
    public void init() {
        this.body = new btRigidBody(templateRigidBody.constructionInfo);
    }
}
