package com.my.demo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.BuilderManager;
import com.my.demo.builder.common.*;
import com.my.demo.builder.scene.test.TestSceneBuilder;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.render.light.GLTFDirectionalLight;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class SceneBuilder {

    public static BuilderManager builderManager = new BuilderManager();

    public static void initBuilderManager(Engine engine) {
        builderManager.scanPackage("com.my.demo.builder");
        builderManager.init(engine);
    }

    public static void initScene(Scene scene) {

        BuilderManager.Instance builder = builderManager.newInstance(scene);

        // ----- Init RenderSystem ----- //

        AssetsManager assetsManager = scene.getEngine().getAssetsManager();
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numPointLights = 30;
        config.numDirectionalLights = 10;
        config.numSpotLights = 10;
        config.numBones = 60;
        PBRShaderProvider shaderProvider = PBRShaderProvider.createDefault(config);
        assetsManager.addAsset("CustomShaderProvider", ShaderProvider.class, shaderProvider);

        SceneRenderableSorter renderableSorter = new SceneRenderableSorter();
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

        builder.build(TestSceneBuilder.class);

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
        scene.addEntity(physicsDebugScriptEntity);
    }

}
