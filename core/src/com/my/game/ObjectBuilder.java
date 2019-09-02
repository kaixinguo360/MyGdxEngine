package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;

public class ObjectBuilder {

    // ----- Static ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final String group = "group";

    // ----- FLAGs ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short AIRCRAFT_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    private World world;
    private PhysicsSystem physicsSystem;
    private ConstraintSystem constraintSystem;


    // ----- Constructor ----- //
    public ObjectBuilder(World world) {
        this.world = world;
        this.physicsSystem = world.getSystem(PhysicsSystem.class);
        this.constraintSystem = world.getSystem(ConstraintSystem.class);
    }


    // ----- Basic Component ----- //

    private int box = 0;
    public Entity createBox(Matrix4 transform, String base) {
        Entity entity = new MyInstance("box", "box");
        addObject(
                "Box-" + box++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }

    private int bomb = 0;
    public Entity createBomb(Matrix4 transform, String base) {
        Entity entity = new MyInstance("bomb", "bomb", null,
                new Collision(BOMB_FLAG, ALL_FLAG, (self, target) -> {
                    if (checkVelocity(self, target, 20)) {
                        System.out.println("Boom! " + self.get(Id.class) + " ==> " + target.get(Id.class));
                        physicsSystem.addExplosion(self.get(Position.class).transform.getTranslation(tmpV1), 5000);
                        world.removeEntity(self);
                    }
                }));
        addObject(
                "Bomb-" + bomb++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }

    private int body = 0;
    public String createBody(Matrix4 transform, String base) {
        return addObject(
                "Body-" + body++,
                transform,
                new MyInstance("body", group),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    private int wing = 0;
    public String createWing(Matrix4 transform, String base) {
        return addObject(
                "Wing-" + wing++,
                transform,
                new MyInstance("wing", group, new Motion.Lift(new Vector3(0, 200, 0)),
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, this::collide)),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint(500)
        );
    }

    private int rotate = 0;
    public String createRotate(Matrix4 transform, ConstraintSystem.Controller controller, String base) {
        Matrix4 relTransform = new Matrix4(world.getEntity(base).get(Position.class).transform).inv().mul(transform);
        String id = addObject(
                "Rotate-" + rotate++,
                transform,
                new MyInstance("rotate", group, null,
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, this::collide)),
                base,
                base == null ? null : new ConstraintSystem.HingeConstraint(
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        constraintSystem.addController(id, base, controller);
        return id;
    }

    private int engine = 0;
    public String createEngine(Matrix4 transform, float force, float maxVelocity, String base) {
        return addObject(
                "Engine-" + engine++,
                transform,
                new MyInstance("engine", group, new Motion.LimitedForce(maxVelocity, new Vector3(0, force, 0)),
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, this::collide)),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }


    // ----- Advanced Component ----- //

    public String createAircraft(Matrix4 transform, float force, float maxVelocity, int upKey, int downKey, int leftKey, int rightKey) {

        String base, last;

        // Body
        base = createBody(transform.cpy().translate(0, 0.5f, -3), null);
        createEngine(transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, base);

        // Left
        last = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90),
                new Controller(-0.15f, 0.2f, 0.5f, rightKey, leftKey), base);
        last = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), last);
        last = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), last);

        // Right
        last = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90),
                new Controller(-0.15f, 0.2f, 0.5f, leftKey, rightKey), base);
        last = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), last);
        last = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), last);

        // Horizontal Tail
        last = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90),
                new Controller(-0.2f, 0.2f, 1f, downKey, upKey), base);
        createWing(transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), last);
        createWing(transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), last);

        // Vertical Tail
        createWing(transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), base);
        createWing(transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), base);

        return base;
    }

    public void createWall(Matrix4 transform, int height) {
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                addObject(
                        "Box-" + box++,
                        tmpM.setToTranslation(tmp + j, 0.5f + i, 0).mulLeft(transform),
                        new MyInstance("box1", "box1"), null, null
                );
            }
        }
    }

    public void createTower(Matrix4 transform, int height) {
        createWall(transform.cpy(), height);
        createWall(transform.cpy().set(transform).translate(0, 0, 10).rotate(Vector3.Y, 90), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 10).rotate(Vector3.Y, 180), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 0).rotate(Vector3.Y, 270), height);
    }


    // ----- Private ----- //

    public String addObject(String id, Matrix4 transform, Entity entity, String base, ConstraintSystem.Config constraint) {
        world.addEntity(id, entity)
                .get(Position.class).transform.set(transform);
        if (base != null) constraintSystem.addConstraint(base, id, constraint);
        return id;
    }
    private void collide(Entity self, Entity target) {
        if (checkVelocity(self, target, 40)) {
            System.out.println("Collision!");
//            constraintSystem.remove(world, self.get(Id.class).id);
        }
    }
    private boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.get(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.get(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }

    private static class Controller implements ConstraintSystem.Controller {
        private float low;
        private float high;
        private float delta;
        private int down;
        private int up;
        private float target = 0;
        private Controller(float low, float high, float delta, int down, int up) {
            this.low = low;
            this.high = high;
            this.delta = delta;
            this.down = down;
            this.up = up;
        }
        @Override
        public void update(btTypedConstraint constraint) {
            if (Gdx.input.isKeyPressed(up)) {
                target += delta;
            } else if (Gdx.input.isKeyPressed(down)) {
                target -= delta;
            } else {
                target += target > 0 ? -delta : (target < 0 ? delta : 0);
            }
            target = target > high ? high : (target < low ? low : target);
            btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
            hingeConstraint.setLimit(target, target, 0, 0.5f);
        }
    }
}
