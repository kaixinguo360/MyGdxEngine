package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;

import java.util.function.Function;

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
        context = Context.obtain(null);
        context.set(AssetsManager.CONTEXT_FIELD_NAME, assetsManager);
        context.set(SerializerManager.CONTEXT_FIELD_NAME, serializerManager);
        context.set(Engine.CONTEXT_FIELD_NAME, this);
    }

    public Context subContext() {
        return this.context.subContext();
    }

    public <T> T subContext(Function<Context, T> fun) {
        return this.context.subContext(fun);
    }

    @Override
    public void dispose() {
        eventManager.dispose();
        sceneManager.dispose();
        assetsManager.dispose();
    }
}
