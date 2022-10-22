package com.my.demo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.EnvironmentSetupScript;
import com.my.demo.scene.AirportScene;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BuilderManager;
import com.my.world.enhanced.builder.EntityRegister;
import com.my.world.enhanced.script.ExitScript;
import com.my.world.enhanced.script.PauseScript;
import com.my.world.enhanced.script.ReloadScript;
import com.my.world.module.common.Position;
import com.my.world.module.particle.ParticlesSystem;
import com.my.world.module.render.light.GLTFDirectionalLight;

public class SceneBuilder {

    public static EntityRegister entityRegister = new EntityRegister();
    public static BuilderManager builderManager = new BuilderManager();

    public static void init(Engine engine) {
        entityRegister.scanPackage("com.my.demo");
        entityRegister.init(engine);
        builderManager.scanPackage("com.my.demo");
        builderManager.init(engine);
    }

    public static void build(Scene scene) {

        BuilderManager.Instance builder = builderManager.newInstance(scene);

        // ----- Init Environments ----- //

        Entity environmentsEntity = new Entity();
        environmentsEntity.setName("environmentsEntity");
        environmentsEntity.addComponent(new Position(new Matrix4()));
        environmentsEntity.addComponent(new EnvironmentSetupScript());
        environmentsEntity.addComponent(scene.getSystemManager().getSystem(ParticlesSystem.class));
        environmentsEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(-0.2f, -0.8f, 1f)));
        environmentsEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(0.2f, 0.8f, -1f)));
        scene.addEntity(environmentsEntity);

        // ----- Init Scene ----- //

        builder.build(AirportScene.class);

        // ----- Init Scripts ----- //

        Entity scriptsEntity = new Entity();
        scriptsEntity.setName("scriptsEntity");
        scriptsEntity.addComponent(new ExitScript());
        scriptsEntity.addComponent(new ReloadScript());
        scriptsEntity.addComponent(new PauseScript());
//        scriptsEntity.addComponent(new PhysicsDebugScript());
        scene.addEntity(scriptsEntity);
    }

}
