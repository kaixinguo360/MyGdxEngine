package com.my.world.enhanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.core.*;
import com.my.world.enhanced.attribute.EnhancedRenderSystem;
import com.my.world.enhanced.entity.EnhancedEntitySerializer;
import com.my.world.gdx.*;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.ConstraintSystem;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.render.EnvironmentSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.List;

public class EnhancedApplication extends GdxApplication {

    @Override
    public void create() {
        Bullet.init();
        Gdx.input.setCursorCatched(true);
        engine = newEngine();
        sceneManager = engine.getSceneManager();
    }

    public Scene newScene() {
        Scene scene = engine.getSceneManager().newScene("default");
        SystemManager systemManager = scene.getSystemManager();
        // Script Module
        systemManager.addSystem(new InputSystem());
        systemManager.addSystem(new ScriptSystem());
        // Physics Module
        systemManager.addSystem(new PhysicsSystem());
        systemManager.addSystem(new ConstraintSystem());
        // Render Module
        systemManager.addSystem(new EnvironmentSystem());
        systemManager.addSystem(new CameraSystem());
        systemManager.addSystem(new EnhancedRenderSystem());
        return scene;
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
