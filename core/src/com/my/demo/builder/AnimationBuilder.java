package com.my.demo.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.animation.*;
import com.my.world.module.animation.clip.Clip;
import com.my.world.module.animation.clip.ClipGroup;
import com.my.world.module.animation.clip.ReverseLoopClip;
import com.my.world.module.common.EnhancedPosition;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.light.GLTFSpotLight;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class AnimationBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        Playable clip1 = assetsManager.addAsset("clip1", Playable.class, createAnimationData1());
        Playable clip2 = assetsManager.addAsset("clip2", Playable.class, createAnimationData2());

        ClipGroup clipGroup1 = new ClipGroup();

        Clip part1 = new Clip();
        part1.playable = clip1;
        part1.start = 0;
        part1.end = 8;
        part1.scale = 2;
        part1.weights = new ConstantCurve<>(0.5f);
        clipGroup1.playables.add(part1);

        Clip part2 = new Clip();
        part2.playable = clip2;
        part2.start = 0;
        part2.end = 8;
        part2.scale = 0.5f;
        part2.weights = new ConstantCurve<>(0.5f);
        clipGroup1.playables.add(part2);

        ReverseLoopClip part3 = new ReverseLoopClip();
        part3.playable = clip2;
        part3.start = 8;
        part3.period = 1f;
        part3.scale = 0.25f;
        part3.reverseRatio = 0.25f;
        clipGroup1.playables.add(part3);

        assetsManager.addAsset("timeline1", Playable.class, clipGroup1);

        TestAnimationController testAnimationController = new TestAnimationController();
        assetsManager.addAsset("testAnimationController", AnimationController.class, testAnimationController);

        scene.createPrefab(AnimationBuilder::createAnimationEntity);
    }

    public static AnimationClip createAnimationData1() {

        AnimationClip clip = new AnimationClip();

        // Channel - root.rotation
        AnimationChannel c1 = new AnimationChannel();
        c1.component = EnhancedPosition.class;
        c1.field = "rotation";
        c1.values = time -> new Vector3(time * 100, time * 100, time * 100);
        clip.channels.add(c1);

        // Channel - child1.rotation
        AnimationChannel c2 = new AnimationChannel();
        c2.entity = "child1";
        c2.component = EnhancedPosition.class;
        c2.field = "rotation";
        c2.values = time -> new Vector3(time * 100, 0, 0);
        clip.channels.add(c2);

        // Channel - child2.rotation
        AnimationChannel c3 = new AnimationChannel();
        c3.entity = "child2";
        c3.component = EnhancedPosition.class;
        c3.field = "rotation";
        c3.values = time -> new Vector3(0, time * 100, 0);
        clip.channels.add(c3);

        // Channel - child2.scale
        AnimationChannel c4 = new AnimationChannel();
        c4.entity = "child2";
        c4.component = EnhancedPosition.class;
        c4.field = "scale";
        c4.values = time -> new Vector3(
                (float) Math.sin(time * 6) * 0.2f + 0.8f,
                (float) Math.sin(time * 2.7) * 0.2f + 0.8f,
                (float) Math.sin(time * 10) * 0.2f + 0.8f
        );
        clip.channels.add(c4);

        // Channel - child2.light.color
        AnimationChannel c5 = new AnimationChannel();
        c5.entity = "child2";
        c5.component = GLTFSpotLight.class;
        c5.field = "light.color";
        c5.values = time -> new Color(
                (float) Math.sin(time * 6) * 0.2f + 0.8f,
                (float) Math.sin(time * 2.7) * 0.2f + 0.8f,
                (float) Math.sin(time * 10) * 0.2f + 0.8f
                , 1f
        );
        clip.channels.add(c5);

        // Channel - child2.light.intensity
        AnimationChannel c6 = new AnimationChannel();
        c6.entity = "child2";
        c6.component = GLTFSpotLight.class;
        c6.field = "light.intensity";
        c6.values = time -> (float) Math.sin(time * 2) * 90 + 90;
        clip.channels.add(c6);

        return clip;
    }

    public static AnimationClip createAnimationData2() {

        AnimationClip clip = new AnimationClip();

        // Channel - root.rotation
        AnimationChannel c1 = new AnimationChannel();
        c1.component = EnhancedPosition.class;
        c1.field = "rotation";
        c1.values = time -> new Vector3((time + 1) * 100, (time + 1) * 100, (time + 1) * 100);
        clip.channels.add(c1);

        // Channel - child1.rotation
        AnimationChannel c2 = new AnimationChannel();
        c2.entity = "child1";
        c2.component = EnhancedPosition.class;
        c2.field = "rotation";
        c2.values = time -> new Vector3((time + 1) * 100, 0, 0);
        clip.channels.add(c2);

        // Channel - child2.rotation
        AnimationChannel c3 = new AnimationChannel();
        c3.entity = "child2";
        c3.component = EnhancedPosition.class;
        c3.field = "rotation";
        c3.values = time -> new Vector3(0, (time + 1) * 100, 0);
        clip.channels.add(c3);

        // Channel - child2.scale
        AnimationChannel c4 = new AnimationChannel();
        c4.entity = "child2";
        c4.component = EnhancedPosition.class;
        c4.field = "scale";
        c4.values = time -> new Vector3(
                (float) Math.sin((time + 1) * 6) * 0.2f + 0.8f,
                (float) Math.sin((time + 1) * 2.7) * 0.2f + 0.8f,
                (float) Math.sin((time + 1) * 10) * 0.2f + 0.8f
        );
        clip.channels.add(c4);

        // Channel - child2.light.color
        AnimationChannel c5 = new AnimationChannel();
        c5.entity = "child2";
        c5.component = GLTFSpotLight.class;
        c5.field = "light.color";
        c5.values = time -> new Color(
                (float) Math.sin((time + 1) * 6) * 0.2f + 0.8f,
                (float) Math.sin((time + 1) * 2.7) * 0.2f + 0.8f,
                (float) Math.sin((time + 1) * 10) * 0.2f + 0.8f
                , 1f
        );
        clip.channels.add(c5);

        // Channel - child2.light.intensity
        AnimationChannel c6 = new AnimationChannel();
        c6.entity = "child2";
        c6.component = GLTFSpotLight.class;
        c6.field = "light.intensity";
        c6.values = time -> (float) Math.sin((time + 1) * 2) * 90 + 90;
        clip.channels.add(c6);

        return clip;
    }

    public static String createAnimationEntity(Scene scene) {
        Entity entity = new Entity();
        entity.setName("AnimationEntity");
        entity.addComponent(new EnhancedPosition(new Matrix4().translate(0, 3, 3)));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("box", GLTFModel.class)));
        entity.addComponent(new BoxBody(new Vector3(0.5f, 0.5f, 0.5f), 0)).isKinematic = true;
        Animation animation = entity.addComponent(new Animation());
        animation.animationController = scene.getAsset("testAnimationController", AnimationController.class);
        animation.addPlayable("clip1", scene.getAsset("clip1", Playable.class));
        animation.addPlayable("clip2", scene.getAsset("clip2", Playable.class));
        animation.addPlayable("clip3", scene.getAsset("timeline1", Playable.class));
        scene.addEntity(entity);

        Entity child1 = new Entity();
        child1.setName("child1");
        child1.setParent(entity);
        child1.addComponent(new EnhancedPosition(new Matrix4().translate(0, 1, 0)));
        child1.addComponent(new BoxBody(new Vector3(0.5f, 0.5f, 0.5f), 0)).isKinematic = true;
        child1.addComponent(new GLTFModelInstance(scene.getAsset("box", GLTFModel.class)));
        scene.addEntity(child1);

        Entity child2 = new Entity();
        child2.setName("child2");
        child2.setParent(child1);
        child2.addComponent(new EnhancedPosition(new Matrix4().translate(1, 0, 0)));
        child2.addComponent(new BoxBody(new Vector3(0.5f, 0.5f, 0.5f), 0)).isKinematic = true;
        child2.addComponent(new GLTFModelInstance(scene.getAsset("box", GLTFModel.class)));
        child2.addComponent(new GLTFSpotLight(Color.WHITE.cpy(), Vector3.Z.cpy(), 60f, 40f));
        scene.addEntity(child2);

        return "AnimationEntity";
    }

    public static class TestAnimationController extends AnimationController {{
        setInitState("state1");
        addState("state1", "clip1");
        addState("state2", "clip2");
        addState("state3", "clip3");
        addTransition(new TestAnimationController.Transition() {{
            this.start = 0;
            this.end = 4;
            this.nextState = "state2";
            this.canSwitch = instance -> Gdx.input.isKeyPressed(Input.Keys.NUM_2);
        }});
        addTransition(new TestAnimationController.Transition() {{
            this.start = 0;
            this.end = 4;
            this.nextState = "state1";
            this.canSwitch = instance -> Gdx.input.isKeyPressed(Input.Keys.NUM_1);
        }});
    }}
}
