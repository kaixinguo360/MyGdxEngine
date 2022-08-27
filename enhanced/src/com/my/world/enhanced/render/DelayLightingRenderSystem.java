package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.my.world.module.render.Render;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_TEST;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class DelayLightingRenderSystem extends EnhancedRenderSystem {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createDiffuse(Color.WHITE));

    protected final EnhancedFrameBuffer fbo;
    protected final PreLightingPassShaderProvider preLightingPassShaderProvider;
    protected final LightingPassShaderProvider anyLightingPassShaderProvider;
    protected final LightingPassShaderProvider pointLightingPassShaderProvider;
    protected final LightingPassShaderProvider directionalLightingPassShaderProvider;
    protected final LightingPassShaderProvider spotLightingPassShaderProvider;

    protected final Array<Renderable> allRenderables = new Array<>();
    protected final Array<Renderable> delayRenderables = new Array<>();
    protected final Array<Renderable> forwardsRenderables = new Array<>();
    protected final Array<Renderable> lightRenderables = new Array<>();
    protected final FlushablePool<Renderable> renderablesPool = new RenderablePool();

    protected final Model sphereModel;
    protected final Model rectModel;
    protected final ModelInstance sphereInstance;
    protected final ModelInstance rectInstance;

    public DelayLightingRenderSystem() {
        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();

        GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(windowWidth, windowHeight);
        frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT);
        frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT);
        frameBufferBuilder.addBasicColorTextureAttachment(Pixmap.Format.RGBA8888);
        frameBufferBuilder.addBasicColorTextureAttachment(Pixmap.Format.RGBA8888);
        frameBufferBuilder.addBasicDepthRenderBuffer();
        frameBufferBuilder.addBasicStencilRenderBuffer();
        fbo = new EnhancedFrameBuffer(frameBufferBuilder);

        preLightingPassShaderProvider = new PreLightingPassShaderProvider(PreLightingPassShaderProvider.createPreLightingPassShaderConfig());
        anyLightingPassShaderProvider = new LightingPassShaderProvider(LightingPassShaderProvider.createAnyLightingPassShaderConfig());
        pointLightingPassShaderProvider = new LightingPassShaderProvider(LightingPassShaderProvider.creatPointLightingPassShaderConfig());
        directionalLightingPassShaderProvider = new LightingPassShaderProvider(LightingPassShaderProvider.createDirectionalLightingPassShaderConfig());
        spotLightingPassShaderProvider = new LightingPassShaderProvider(LightingPassShaderProvider.creatSpotLightingPassShaderConfig());

        Environment environment = new Environment();
        environment.set(GBufferAttribute.createGBuffer0(fbo.getTextureAttachments().get(0)));
        environment.set(GBufferAttribute.createGBuffer1(fbo.getTextureAttachments().get(1)));
        environment.set(GBufferAttribute.createGBuffer2(fbo.getTextureAttachments().get(2)));
        environment.set(GBufferAttribute.createGBuffer3(fbo.getTextureAttachments().get(3)));

        ModelBuilder mBuilder = new ModelBuilder();

        sphereModel = mBuilder.createSphere(1, 1, 1, 16, 16, GL_TRIANGLES, MATERIAL, ATTRIBUTES);
        sphereInstance = new ModelInstance(sphereModel);
//        sphereInstance.getRenderables(lights, renderablesPool);

        rectModel = mBuilder.createRect(-1, -1, -10, 1, -1, -10, 1, 1, -10, -1, 1, -10, 0, 0, 0, MATERIAL, ATTRIBUTES);
        rectInstance = new ModelInstance(rectModel);
        rectInstance.getRenderables(lightRenderables, renderablesPool);

        for (Renderable renderable : lightRenderables) {
            renderable.environment = environment;
        }
    }

    // ----- RenderBatch ----- //

    @Override
    public void beginBatch(Camera cam) {
        if (currentCamera != null) throw new RuntimeException("Current camera is not null");
        currentCamera = cam;
    }

    @Override
    public void endBatch() {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        classification();
        lightingRender();
//        forwardRender();
        renderablesPool.flush();
        allRenderables.clear();
        delayRenderables.clear();
        forwardsRenderables.clear();
        lightRenderables.clear();
        currentCamera = null;
    }

    protected void classification() {
        for (int i = 0; i < allRenderables.size; i++) {
            final Renderable renderable = allRenderables.get(i);
            if (canDelayRender(renderable)) {
                delayRenderables.add(renderable);
            } else {
                forwardsRenderables.add(renderable);
            }
        }
    }

    protected boolean canDelayRender(Renderable renderable) {
        Shader shader = preLightingPassShaderProvider.getShader(renderable);
        return shader instanceof LightingPassShader;
    }

    @Override
    public void addToBatch(Render render) {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        if (!render.isVisible(currentCamera)) {
            return;
        }
        final int offset = allRenderables.size;
        render.getRenderables(allRenderables, renderablesPool);
        for (int i = offset; i < allRenderables.size; i++) {
            Renderable renderable = allRenderables.get(i);
            if (currentEnvironment != null && render.includeEnv) {
                renderable.environment = currentEnvironment;
            }
            if (render.shader != null) {
                renderable.shader = render.shader;
            }
        }
    }

    // ----- Render ----- //

    protected void lightingRender() {
        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        render(delayRenderables, batch.getRenderContext(), batch.getRenderableSorter(), preLightingPassShaderProvider, currentCamera);
        fbo.end();

        PointLightsAttribute pointLights = currentEnvironment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (pointLights != null) {
            for (PointLight light : pointLights.lights) {
                float range = 50;
                sphereInstance.transform.setToTranslation(light.position).scl(range);
                sphereInstance.getRenderables(lightRenderables, renderablesPool);

                Renderable lightRenderable = lightRenderables.peek();
                lightRenderable.material.set(lightRenderable.environment);
                PointLightsAttribute lightsAttribute = new PointLightsAttribute();
                lightsAttribute.lights.add(light);
                lightRenderable.material.set(lightsAttribute);
            }
        }

        Gdx.gl.glDisable(GL_DEPTH_TEST);
        render(lightRenderables, batch.getRenderContext(), batch.getRenderableSorter(), anyLightingPassShaderProvider, currentCamera);
        Gdx.gl.glEnable(GL_DEPTH_TEST);
    }

    protected void forwardRender() {
        render(forwardsRenderables, batch.getRenderContext(), batch.getRenderableSorter(), batch.getShaderProvider(), currentCamera);
    }

    public static void render(Array<Renderable> renderables, RenderContext context, RenderableSorter sorter, ShaderProvider shaderProvider, Camera camera) {
        if (context != null) context.begin();
        if (sorter != null) sorter.sort(camera, renderables);
        Shader currentShader = null;
        for (int i = 0; i < renderables.size; i++) {
            final Renderable renderable = renderables.get(i);
            Shader shader = shaderProvider.getShader(renderable);
            if (currentShader != shader) {
                if (currentShader != null) currentShader.end();
                currentShader = shader;
                currentShader.begin(camera, context);
            }
            currentShader.render(renderable);
        }
        if (currentShader != null) currentShader.end();
        if (context != null) context.end();
    }

    public static class RenderablePool extends FlushablePool<Renderable> {

        @Override
        protected Renderable newObject() {
            return new Renderable();
        }

        @Override
        public Renderable obtain() {
            Renderable renderable = super.obtain();
            renderable.environment = null;
            renderable.material = null;
            renderable.meshPart.set("", null, 0, 0, 0);
            renderable.shader = null;
            renderable.userData = null;
            return renderable;
        }
    }
}
