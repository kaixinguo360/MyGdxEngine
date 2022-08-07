package com.my.world.enhanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.core.ConfigurableSerializer;
import com.my.world.core.Engine;
import com.my.world.core.SceneSerializer;
import com.my.world.core.Serializer;
import com.my.world.enhanced.entity.EnhancedEntitySerializer;
import com.my.world.gdx.*;

import java.util.List;

public class EnhancedApplication extends GdxApplication {

    @Override
    public void create() {
        Bullet.init();
        Gdx.input.setCursorCatched(true);
        engine = newEngine();
        sceneManager = engine.getSceneManager();
    }

    public static Engine newEngine() {
        Engine engine = new Engine();
        List<Serializer> serializers = engine.getSerializerManager().getSerializers();
        serializers.add(new SceneSerializer());
        serializers.add(new Matrix4Serializer());
        serializers.add(new Vector3Serializer());
        serializers.add(new QuaternionSerializer());
        serializers.add(new ColorSerializer());
        serializers.add(new Vector2Serializer());
        serializers.add(new TypeSerializer());
        serializers.add(new EnhancedEntitySerializer());
        serializers.add(new ConfigurableSerializer());
        return engine;
    }
}
