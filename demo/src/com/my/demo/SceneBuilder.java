package com.my.demo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.EnvironmentSetupScript;
import com.my.demo.scene.AirportSceneBuilder;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.attribute.MyRenderableSorter;
import com.my.world.enhanced.attribute.MyShaderProvider;
import com.my.world.enhanced.builder.BuilderManager;
import com.my.world.enhanced.builder.EntityRegister;
import com.my.world.enhanced.script.ExitScript;
import com.my.world.enhanced.script.PauseScript;
import com.my.world.enhanced.script.PhysicsDebugScript;
import com.my.world.enhanced.script.ReloadScript;
import com.my.world.module.common.Position;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.render.light.GLTFDirectionalLight;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

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

        // ----- Init RenderSystem ----- //

        AssetsManager assetsManager = scene.getEngine().getAssetsManager();
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numPointLights = 30;
        config.numDirectionalLights = 10;
        config.numSpotLights = 10;
        config.numBones = 24;
        PBRShaderProvider shaderProvider = new MyShaderProvider(config);
        assetsManager.addAsset("CustomShaderProvider", ShaderProvider.class, shaderProvider);

        SceneRenderableSorter renderableSorter = new MyRenderableSorter();
        assetsManager.addAsset("CustomRenderableSorter", RenderableSorter.class, renderableSorter);

        RenderSystem renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
        renderSystem.shaderProvider = shaderProvider;
        renderSystem.renderableSorter = renderableSorter;

        // ----- Init Environments ----- //

        Entity lightEntity = new Entity();
//        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.AmbientLight, new Color(0.4f, 0.4f, 0.4f, 1f)));
//        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Fog, new Color(0.8f, 0.8f, 0.8f, 1f)));
        lightEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(-0.2f, -0.8f, 1f)));
        lightEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(0.2f, 0.8f, -1f)));
        scene.getEntityManager().addEntity(lightEntity);

        Entity environmentEntity = new Entity();
        environmentEntity.setName("environmentEntity");
        environmentEntity.addComponent(new Position(new Matrix4()));
        environmentEntity.addComponent(new EnvironmentSetupScript());
        scene.addEntity(environmentEntity);

        // ----- Init Scene ----- //

        builder.build(AirportSceneBuilder.class);

        // ----- Init Scripts ----- //

        Entity exitScriptEntity = new Entity();
        exitScriptEntity.setName("exitScriptEntity");
        exitScriptEntity.addComponent(new ExitScript());
        scene.addEntity(exitScriptEntity);

        Entity reloadScriptEntity = new Entity();
        reloadScriptEntity.setName("reloadScriptEntity");
        reloadScriptEntity.addComponent(new ReloadScript());
        scene.addEntity(reloadScriptEntity);

        Entity pauseScriptEntity = new Entity();
        pauseScriptEntity.setName("pauseScriptEntity");
        pauseScriptEntity.addComponent(new PauseScript());
        scene.addEntity(pauseScriptEntity);

        Entity physicsDebugScriptEntity = new Entity();
        physicsDebugScriptEntity.setName("physicsDebugScriptEntity");
        physicsDebugScriptEntity.addComponent(new PhysicsDebugScript());
//        scene.addEntity(physicsDebugScriptEntity);
    }

}
