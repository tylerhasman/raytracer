package me.tyler.raytrace;

public class Start {

    public static void main(String[] args){
        Window window = new Window(new RaytraceGame(), 640 * 2, 480 * 2, "Game");

        window.run();
    }

}
