package me.tyler.raytrace;

import javafx.scene.shape.Circle;

import java.awt.*;

public class Light {

    public float x, z;
    public float intensity;
    public float size;
    public float r, g, b;

    public Light(float x, float z, float intensity, float size, float r, float g, float b) {
        this.x = x;
        this.z = z;
        this.size = size;
        this.intensity = intensity;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public boolean inRange(float x, float z){
        return dst(x, z) < size;
    }

    public float dst(float x, float z){
        return (float) Math.sqrt(((this.x - x) * (this.x - x) + (this.z - z) * (this.z - z)));
    }

}
