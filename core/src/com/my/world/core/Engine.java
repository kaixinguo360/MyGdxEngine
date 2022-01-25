package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;

public class Engine implements Disposable {

    public static final String CONTEXT_FIELD_NAME = "ENGINE";

    @Getter
    protected final AssetsManager assetsManager;

    @Getter
    protected final SerializerManager serializerManager;

    @Getter
    protected final SceneManager sceneManager;

    @Getter
    protected final JarManager jarManager;

    @Getter
    private final EventManager eventManager = new EventManager();

    @Getter
    protected final Context context;

    public Engine() {
        assetsManager = new AssetsManager(this);
        serializerManager = new SerializerManager(this);
        sceneManager = new SceneManager(this);
        jarManager = new JarManager(this);
        context = new Context(null);
        context.setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, assetsManager);
        context.setEnvironment(SerializerManager.CONTEXT_FIELD_NAME, serializerManager);
        context.setEnvironment(Engine.CONTEXT_FIELD_NAME, this);
    }

    public Context newContext() {
        return this.context.newContext();
    }

    @Override
    public void dispose() {
        eventManager.dispose();
        sceneManager.dispose();
        assetsManager.dispose();
    }
}
