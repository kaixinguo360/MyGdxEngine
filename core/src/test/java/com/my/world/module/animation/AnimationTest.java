package com.my.world.module.animation;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.my.demo.builder.AnimationBuilder;
import com.my.world.core.Engine;
import com.my.world.core.SerializerManager;
import com.my.world.gdx.GdxApplication;
import com.my.world.module.animation.clip.Clip;
import com.my.world.module.animation.clip.ClipGroup;
import com.my.world.module.animation.clip.ReverseLoopClip;
import com.my.world.module.common.EnhancedPosition;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimationTest {

    @Test
    public void testAnimationClip() {
        testSerialize(createAnimationClip());
    }

    @Test
    public void testClipGroup() {
        AnimationClip clip = createAnimationClip();
        ClipGroup clipGroup = new ClipGroup();

        Clip part1 = new Clip();
        part1.playable = clip;
        part1.start = 0;
        part1.end = 4;
        clipGroup.playables.add(part1);

        Clip part2 = new Clip();
        part2.playable = clip;
        part2.start = 4;
        part2.end = 8;
        clipGroup.playables.add(part2);

        ReverseLoopClip part3 = new ReverseLoopClip();
        part3.playable = clip;
        part3.start = 8;
        part3.period = 1f;
        part3.reverseRatio = 0.25f;
        clipGroup.playables.add(part3);

        testSerialize(clipGroup);
    }

    @Test
    public void testAnimationController() {
        testSerialize(new AnimationBuilder.TestAnimationController());
    }

    @Test
    public void testDrawCurve() {

        Screen screen = new Screen(35, 20, 50, 11, 0.25f, 0.25f);
//        screen.drawCo();
        screen.drawBox();

        // Create Bezier
        List<Vector2> points = new ArrayList<Vector2>(){{
            add(new Vector2(10.0f, 0.999538004398346f));
            add(new Vector2(165.35191345214844f, 84.43067932128906f));
            add(new Vector2(-69.90814208984375f, 73.34440612792969f));
            add(new Vector2(61.0f, 0.9199790954589844f));
            add(new Vector2(72.05024719238281f, -5.1935272216796875f));
            add(new Vector2(93.66666412353516f, 35.379310607910156f));
            add(new Vector2(110.0f, 35.379310607910156f));
        }};

        // Create BezierCurve
        BezierCurve curves = new BezierCurve(points);

        float range = 120;
        int pointNum = 500;
        for (int i = 0; i < pointNum; i++) {
            float time = (range * i) / pointNum;
            float value = curves.valueAt(time);
            screen.draw(time, value, '.');
        }

        drawBezier(screen, points, 0);
        drawBezier(screen, points, 3);

        System.out.println(screen);
    }

    public static void testSerialize(Object obj) {
        // Init
        Engine engine = GdxApplication.newEngine();
        SerializerManager serializerManager = engine.getSerializerManager();

        // Test Dump
        Map config = serializerManager.dump(obj, Map.class, engine.getContext());
        System.out.println(config);

        Yaml yaml = new Yaml();
        String yamlConfig = yaml.dump(config);
        System.out.println(yamlConfig);

        // Test Load
        config = yaml.loadAs(yamlConfig, Map.class);
        Object result = serializerManager.load(config, obj.getClass(), engine.getContext());
        System.out.println(result);
    }

    public static AnimationClip createAnimationClip() {

        // Create Bezier
        List<Vector2> points = new ArrayList<Vector2>(){{
            add(new Vector2(10.0f, 0.999538004398346f));
            add(new Vector2(52.28984069824219f, 7.935822010040283f));
            add(new Vector2(21.75360870361328f, 5.401725769042969f));
            add(new Vector2(61.0f, 0.9199790954589844f));
        }};

        // Create AnimationChannel
        AnimationChannel channel = new AnimationChannel();
        channel.entity = "A";
        channel.component = EnhancedPosition.class;
        channel.field = "B";
        channel.values = new BezierCurve(points);

        // Create AnimationClip
        AnimationClip clip = new AnimationClip();
        clip.channels.add(channel);

        return clip;
    }

    public static void drawBezier(Screen screen, List<Vector2> points, int index) {
        final Vector2 p1 = new Vector2();
        final Vector2 p2 = new Vector2();
        final Vector2 p3 = new Vector2();
        final Vector2 p4 = new Vector2();

        p1.set(points.get(index + 0));
        p2.set(points.get(index + 1));
        p3.set(points.get(index + 2));
        p4.set(points.get(index + 3));

        BezierCurve.fcurveCorrect(p1, p2, p3, p4);

        for (int i = 0; i < 100; i++) {
            float time = i / 100f;
            Vector2 cubic = Bezier.cubic(new Vector2(), time, p1, p2, p3, p4, new Vector2());
            screen.draw(cubic.x, cubic.y, 'X');
        }
    }
}
