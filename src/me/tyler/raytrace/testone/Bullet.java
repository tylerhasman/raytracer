package me.tyler.raytrace.testone;

import me.tyler.raytrace.FastMath;

import java.awt.*;

public class Bullet extends SphereObject{

    public float vx, vz;

    public Bullet(float x, float z, float angle) {
        super(x, 0.5f, z, Color.BLACK.getRGB(), 0.1f);
        vx = -FastMath.cos(angle);
        vz = -FastMath.sin(angle);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        x += vx * delta;
        z += vz * delta;
    }
}
