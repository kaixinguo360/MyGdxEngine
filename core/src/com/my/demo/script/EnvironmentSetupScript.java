package com.my.demo.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.render.EnvironmentSystem;
import com.my.world.module.script.ScriptSystem;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class EnvironmentSetupScript implements ScriptSystem.OnStart {

    private Cubemap environmentCubemap;
    private Cubemap diffuseCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;

    @Override
    public void start(Scene scene, Entity entity) {
        EnvironmentSystem environmentSystem = scene.getSystemManager().getSystem(EnvironmentSystem.class);
        Environment commonEnvironment = environmentSystem.getCommonEnvironment();

        // setup light
        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        commonEnvironment.set(ColorAttribute.createAmbient(1f, 1f, 1f, 1));
        commonEnvironment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        commonEnvironment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        commonEnvironment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
    }

    @Override
    public void dispose() {
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
    }
}
