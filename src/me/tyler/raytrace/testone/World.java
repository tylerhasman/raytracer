package me.tyler.raytrace.testone;

import java.util.ArrayList;
import java.util.List;

public class World {

    private int[][] tiles;
    private List<Light> lights;
    private List<WorldObject> worldObjects;

    public World(int width, int length){
        tiles = new int[width][length];
        lights = new ArrayList<>();
        worldObjects = new ArrayList<>();
    }

    public Tile getTile(int x, int z){
        return Tile.TILES[getTileId(x, z)];
    }

    public int getTileId(int x, int z){
        if(x < 0 || z < 0 || x >= tiles.length || z >= tiles[0].length)
            return 0;
        return tiles[x][z];
    }

    public void setTileId(int x, int z, int id){
        if(x < 0 || z < 0 || x >= tiles.length || z >= tiles[0].length)
            return;
        tiles[x][z] = id;
    }

    public void addLight(Light light){
        lights.add(light);
    }

    public void addObject(WorldObject worldObject){
        worldObjects.add(worldObject);
    }

    public List<WorldObject> getWorldObjects() {
        return worldObjects;
    }

    public List<Light> getLights() {
        return lights;
    }

    public void update(float delta) {
        for(WorldObject worldObject : worldObjects)
            worldObject.update(delta);
    }

}
