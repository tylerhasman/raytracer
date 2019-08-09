package me.tyler.raytrace;

public class CubeObject extends WorldObject {

    public float width, height, length;

    public CubeObject(float x, float y, float z, int color, float width, float height, float length) {
        super(x, y, z, color);
        this.width = width;
        this.height = height;
        this.length = length;
    }

    @Override
    public boolean intersects(float x, float y, float z) {

        if(x >= this.x && x <= this.x + width){
            if(y >= this.y && y <= this.y + height){
                if(z >= this.z && z <= this.z + length) {
                    return true;
                }
            }
        }

        return false;
    }
}
