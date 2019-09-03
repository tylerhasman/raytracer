package me.tyler.raytrace;

public class SphereObject extends WorldObject {

    public float radius;

    public SphereObject(float x, float y, float z, int color, float radius) {
        super(x, y, z, color);
        this.radius = radius;
    }

    @Override
    public boolean intersects(float x, float y, float z) {
        float dst = (float) Math.sqrt(((this.x - x) * (this.x - x) + (this.z - z) * (this.z - z)) + (this.y - y) * (this.y - y));

        return dst <= radius;
    }

}
