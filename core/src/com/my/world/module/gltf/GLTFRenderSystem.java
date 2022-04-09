package com.my.world.module.gltf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.gdx.DisposableManager;
import com.my.world.module.common.Position;
import com.my.world.module.common.RenderSystem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

@NoArgsConstructor
public class GLTFRenderSystem implements RenderSystem, System.AfterAdded, Configurable.OnInit {

    @Config
    protected boolean useDefaultEnvironment;

    @Getter
    protected final Environment commonEnvironment = new Environment();

    @Getter
    protected SceneManager sceneManager;

    protected final DisposableManager disposableManager = new DisposableManager();

    public GLTFRenderSystem(boolean useDefaultEnvironment) {
        this.useDefaultEnvironment = useDefaultEnvironment;
        init();
    }

    @Override
    public void init() {

        // setup sceneManager
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numPointLights = 30;
        config.numDirectionalLights = 10;
        config.numSpotLights = 10;
        config.numBones = 24;
        PBRShaderProvider shaderProvider = PBRShaderProvider.createDefault(config);
        sceneManager = new SceneManager(shaderProvider, PBRShaderProvider.createDefaultDepth(config.numBones));
        disposableManager.addDisposable(sceneManager);

        if (!useDefaultEnvironment) return;

        // setup light
        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        Cubemap environmentCubemap = iblBuilder.buildEnvMap(1024);
        Cubemap diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        Cubemap specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        Texture brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        commonEnvironment.set(ColorAttribute.createAmbient(1f, 1f, 1f, 1));
        commonEnvironment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        commonEnvironment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        commonEnvironment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        SceneSkybox skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        disposableManager.addDisposable(environmentCubemap);
        disposableManager.addDisposable(diffuseCubemap);
        disposableManager.addDisposable(specularCubemap);
        disposableManager.addDisposable(brdfLUT);
        disposableManager.addDisposable(skybox);
    }

    @Override
    public void dispose() {
        disposableManager.dispose();
    }

    // ----- EntityListener ----- //

    protected EntityManager entityManager;
    protected final EntityFilter renderFilter = entity -> entity.contains(GLTFRender.class);
    protected final EntityFilter lightFilter = entity -> entity.contains(GLTFLight.class);

    @Override
    public void afterAdded(Scene scene) {
        entityManager = scene.getEntityManager();
        entityManager.addFilter(renderFilter);
        entityManager.addFilter(lightFilter);
    }

    // ----- RenderSystem ----- //

    @Override
    public void begin() {

        // Clear Render & Light Component
        sceneManager.getRenderableProviders().clear();
        sceneManager.environment.clear();

        // Add Light Component
        sceneManager.environment.set(commonEnvironment);
        for (Entity entity : entityManager.getEntitiesByFilter(lightFilter)) {
            for (GLTFLight component : entity.getComponents(GLTFLight.class)) {
                if (component.isActive()) {
                    sceneManager.environment.add(component.getLight());
                }
            }
        }

        // Add Render Component
        for (Entity entity : entityManager.getEntitiesByFilter(renderFilter)) {
            Position position = entity.getComponent(Position.class);
            for (GLTFRender component : entity.getComponents(GLTFRender.class)) {
                if (component.isActive()) {
                    component.scene.modelInstance.transform.set(position.getGlobalTransform());
                    sceneManager.addScene(component.scene);
                }
            }
        }
    }

    @Override
    public void render(PerspectiveCamera cam) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.setCamera(cam);
        sceneManager.update(Gdx.graphics.getDeltaTime());
        sceneManager.render();
    }

    @Override
    public void end() {

    }
}
