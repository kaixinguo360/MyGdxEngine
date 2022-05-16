package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.utils.Array;
import com.my.world.module.animation.*;
import com.my.world.module.common.EnhancedPosition;

public class GdxAnimationUtil {

    public static boolean hasAnimation(Model model) {
        return model.animations != null && model.animations.size > 0;
    }

    public static Animation getAnimation(Array<com.badlogic.gdx.graphics.g3d.model.Animation> gdxAnimations) {
        if (gdxAnimations == null) throw new RuntimeException("gdxAnimations is null");
        if (gdxAnimations.size == 0) throw new RuntimeException("gdxAnimations is empty");

        Animation animation = new Animation();
        DefaultAnimationController controller = new DefaultAnimationController();

        animation.useLocalTime = true;
        animation.animationController = controller;

        for (com.badlogic.gdx.graphics.g3d.model.Animation gdxAnimation : gdxAnimations) {
            String id = gdxAnimation.id;
            id = id.replaceAll("^([^\\[]*)\\[.*$", "$1");
            AnimationClip clip = getAnimationClip(gdxAnimation);
            animation.addPlayable(id, clip);
            controller.addState(id, id);
        }

        if (!animation.playables.containsKey("default")) {
            animation.addPlayable("default", new AnimationClip());
            controller.addState("default", "default");
        }
        controller.initState = "default";

        return animation;
    }

    public static AnimationClip getAnimationClip(com.badlogic.gdx.graphics.g3d.model.Animation gdxAnimation) {
        AnimationClip clip = new AnimationClip();
        for (NodeAnimation nodeAnimation : gdxAnimation.nodeAnimations) {
            String id = nodeAnimation.node.id;
            id = id.replaceAll("^([^\\[]*)\\[.*$", "$1");
            if (true) {
                AnimationChannel channel = new AnimationChannel();
                channel.entity = id;
                channel.component = EnhancedPosition.class;
                channel.index = 0;
                channel.field = "isDirect";
                channel.values = new ConstantCurve<>(true);
                clip.channels.add(channel);
            }
            if (nodeAnimation.translation != null) {
                AnimationChannel channel = new AnimationChannel();
                channel.entity = id;
                channel.component = EnhancedPosition.class;
                channel.index = 0;
                channel.field = "translation";
                channel.values = new GdxVector3Curve(nodeAnimation.translation);
                clip.channels.add(channel);
            }
            if (nodeAnimation.rotation != null) {
                AnimationChannel channel = new AnimationChannel();
                channel.entity = id;
                channel.component = EnhancedPosition.class;
                channel.index = 0;
                channel.field = "orientation";
                channel.values = new GdxQuaternionCurve(nodeAnimation.rotation);
                clip.channels.add(channel);
            }
            if (nodeAnimation.scaling != null) {
                AnimationChannel channel = new AnimationChannel();
                channel.entity = id;
                channel.component = EnhancedPosition.class;
                channel.index = 0;
                channel.field = "scale";
                channel.values = new GdxVector3Curve(nodeAnimation.scaling);
                clip.channels.add(channel);
            }
        }
        return clip;
    }
}
