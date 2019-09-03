package me.tyler.raytrace;

public class Tile {

    public static Tile[] TILES = new Tile[255];

    public static final Tile AIR = new Tile(0);
    public static final Tile DIRT = new Tile(1).setSolid(true).setTextureRef("dirt");
    public static final Tile SKY = new Tile(2);
    public static final Tile GROUND = new Tile(3).setTextureRef("dirt");
    public static final Tile BRICKS = new Tile(4).setTextureRef("bricks");

    private boolean solid;

    private final int id;

    private String textureRef;

    private Tile(int id){
        this.id = id;
        TILES[id] = this;
        solid = false;
    }

    private Tile setTextureRef(String textureRef){
        this.textureRef = textureRef;
        return this;
    }

    private Tile setSolid(boolean solid){
        this.solid = solid;
        return this;
    }

    public boolean isSolid(){
        return solid;
    }

    public int getId() {
        return id;
    }

    public String getTextureRef() {
        return textureRef;
    }
}
