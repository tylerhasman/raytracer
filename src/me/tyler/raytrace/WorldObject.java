package me.tyler.raytrace;

public abstract class WorldObject {

    public float x;
    public float y;
    public float z;
    public int color;

    public WorldObject(float x, float y, float z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public abstract boolean intersects(float x, float y, float z);

    public void update(float delta){

    }
}
