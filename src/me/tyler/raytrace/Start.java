package me.tyler.raytrace;

import me.tyler.raytrace.testone.RaytraceGame;

public class Start {

    public static void main(String[] args){
        Window window = new Window(new RaytraceGame(), 1024, 720, "Game");

        window.run();
    }

}
