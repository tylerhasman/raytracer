package me.tyler.raytrace.testone;

import me.tyler.raytrace.FastMath;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class WorldRenderTask implements Runnable {

    private BufferedImage section;
    private int[] pixelData;

    private float cameraX, cameraY, cameraZ;
    private int sectionX, sectionY;

    private float fov;

    private float cameraYaw, cameraPitch;

    private float drawDistance;

    private World world;

    private float totalWidth, totalHeight;

    private RaytraceGame raytraceGame;

    public WorldRenderTask(int x, int y, int width, int height, float tw, float th, RaytraceGame raytraceGame){
        section = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelData = new int[width * height];
        sectionX = x;
        sectionY = y;
        totalWidth = tw;
        totalHeight = th;
        this.raytraceGame = raytraceGame;
    }

    public int getSectionX() {
        return sectionX;
    }

    public int getSectionY() {
        return sectionY;
    }

    public BufferedImage getRenderedImage() {
        return section;
    }

    public void updateCamera(float x, float y, float z, float cameraYaw, float cameraPitch, float fov, float drawDistance, World world){
        cameraX = x;
        cameraZ = z;
        cameraY = y;
        this.fov = fov;
        this.cameraPitch = cameraPitch;
        this.cameraYaw = cameraYaw;
        this.drawDistance = drawDistance;
        this.world = world;
    }

    @Override
    public void run() {
        CalculatedSide calculatedSide = new CalculatedSide();
        for(float i = sectionX; i < section.getWidth() + sectionX;i++){

            float rayScreenX = 0.5f - i / totalWidth;

            float rayScreenXFov = (rayScreenX * fov);

            float rayVelX = FastMath.cos((float) Math.toRadians((rayScreenXFov + cameraYaw)));
            float rayVelZ = FastMath.sin((float) Math.toRadians((rayScreenXFov + cameraYaw)));

            float rayWorldX = cameraX;
            float rayWorldZ = cameraZ;

            for(float j = sectionY; j < section.getHeight() + sectionY;j++){

                float rayScreenY = 0.5f - j / totalHeight;

                float rayWorldY = (rayScreenY / fov) + cameraY;

                float rayVelY = FastMath.sin((float) Math.toRadians((rayScreenY * (fov / 2f) + cameraPitch)));

                Tile seenTile = null;
                float hitRayProjX = 0.5f;
                float hitRayProjY = 0.5f;
                WorldObject seenObject = null;

                float rayDistance = 0f;

                float lightR = 0, lightG = 0, lightB = 0;

                while(rayDistance < drawDistance){

                    calcNextSide(rayWorldX + rayVelX * rayDistance, rayWorldZ + rayVelZ * rayDistance, rayVelX, rayVelZ, calculatedSide);
                    rayDistance += calculatedSide.additionalDistance;

                    float checkX = rayWorldX + rayVelX * rayDistance;
                    float checkY = rayWorldY + rayVelY * rayDistance;
                    float checkZ = rayWorldZ + rayVelZ * rayDistance;

                    int tileX = (int) checkX;
                    int tileZ = (int) checkZ;

                    int tile = world.getTileId(tileX, tileZ);

                    /*for(Light light : world.getLights()){
                        float dst = light.dst(checkX, checkZ);
                        if(dst <= light.size){
                            lightR += (light.r * light.intensity * (1f - dst / light.size));
                            lightG += (light.g * light.intensity * (1f - dst / light.size));
                            lightB += (light.b * light.intensity * (1f - dst / light.size));
                        }
                    }*/


                    if(checkY > 1F){
                        if(rayWorldY < 1F)
                            tile = Tile.SKY.getId();
                        else//Only render sky if we are under sky
                            continue;
                    }else if(checkY < 0F)
                        if(rayWorldY > 0F)
                            tile = Tile.GROUND.getId();
                        else//Only render ground if we are above ground
                            continue;

                    /*for(WorldObject worldObject : world.getWorldObjects()){
                        if(worldObject.intersects(checkX, checkY, checkZ)){
                            seenObject = worldObject;
                            break;
                        }
                    }

                    if(seenObject != null)
                        break;*/

                    if(tile != 0) {
                        seenTile = Tile.TILES[tile];

                        hitRayProjY = (float) (checkY - Math.floor(checkY));

                        float fractX = (float) (checkX - Math.floor(checkX));
                        float fractZ = (float) (checkZ - Math.floor(checkZ));

                        hitRayProjX = fractX;

                        if(fractX < 0.02f){
                            hitRayProjX = fractZ;
                        }else if(fractZ < 0.02f){
                            hitRayProjX = fractX;
                        }

                        if(fractX >= 0.98f){
                            hitRayProjX = fractZ;
                        }else if(fractZ >= 0.98f){
                            hitRayProjX = fractX;
                        }

                        break;
                    }


                }

                float dstX = rayVelX * rayDistance;
                float dstZ = rayVelZ * rayDistance;
                float dstY = rayVelY * rayDistance;

                float dst = 1f;//(float) (1f - Math.min(1f, Math.sqrt(dstX * dstX + dstZ * dstZ + dstY * dstY) / drawDistance)) / 2f;

                int drawnColor = 0x000000;

                if(seenObject != null) {
                    drawnColor = seenObject.color;
                }else if(seenTile == null){
                    drawnColor = Color.LIGHT_GRAY.getRGB();
                }else if(seenTile.equals(Tile.SKY)) {
                    drawnColor = 0x51B6FF;
                }else if(seenTile.equals(Tile.GROUND)) {
                    drawnColor = 0xB29400;
                }else{
                    BufferedImage texture = raytraceGame.getTextures().get(seenTile.getTextureRef());
                    if(texture != null){
                        drawnColor = texture.getRGB((int) (hitRayProjX * texture.getWidth()), (int) (hitRayProjY * texture.getHeight()));
                    }
                    //drawnColor = new Color(hitRayProjX, 0, 0, 1f).getRGB();
                }

                int r = (drawnColor >> 16) & 0xFF;
                int g = (drawnColor >> 8) & 0xFF;
                int b = (drawnColor) & 0xFF;

                r *= dst / 2f;
                g *= dst / 2f;
                b *= dst / 2f;

                r += lightR;
                g += lightG;
                b += lightB;

                int packed = ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF));

                pixelData[(int) ((i - sectionX) + (j - sectionY) * section.getWidth())] = packed;
            }
        }
        section.getRaster().setDataElements(0, 0, section.getWidth(), section.getHeight(), pixelData);
    }

    private static void calcNextSide(float checkX, float checkZ, float rayVelX, float rayVelZ, CalculatedSide out) {

        float p_x = FastMath.fract(checkX);
        float p_z = FastMath.fract(checkZ);

        // d = vt
        // d/v = t

        float d_x = (rayVelX > 0 ? (1F - p_x) : (0F - p_x));
        float d_z = (rayVelZ > 0 ? (1F - p_z) : (0F - p_z));

        float EPSILON = 0.0001f;

        if(rayVelZ == 0.0F || d_z == 0f){
            out.hitX = true;
            out.additionalDistance = Math.abs(d_x / rayVelX) + EPSILON;
        }else  if(rayVelX == 0.0F || d_x == 0f) {
            out.hitX = false;
            out.additionalDistance = Math.abs(d_z / rayVelZ) + EPSILON;
        }else{
            float calcX = Math.abs(d_x / rayVelX);
            float calcZ = Math.abs(d_z / rayVelZ);
            out.hitX = calcX < calcZ;
            out.additionalDistance = Math.min(calcX, calcZ) + EPSILON;
        }

    }

    private static class CalculatedSide {
        public float additionalDistance;
        public boolean hitX;//if false hit Z
    }

    public static void main(String[] args){
/*        Random random = new Random();

        for(int i = 0; i < 1000;i++){
            float c_x = random.nextFloat() + random.nextInt(1000);
            float c_z = random.nextFloat() + random.nextInt(1000);

            float angle = (float) (random.nextFloat() * Math.PI * 2);

            float v_x = (float) Math.cos(angle);
            float v_z = (float) Math.sin(angle);

            float calc = calcNextSide(c_x, c_z, v_x, v_z);

            float px = (c_x + v_x * calc);
            float pz = (c_z + v_z * calc);

            if((px - Math.floor(px)) != 0.0F && (pz - Math.floor(pz)) != 0.0F)
            {
                System.err.println("Failed with "+c_x+" "+c_z+" "+v_x+" "+v_z+" got "+calc+" for "+px+"/"+pz);
                calcNextSide(c_x, c_z, v_x, v_z);
            }else{
                System.out.println("Passed with "+c_x+" "+c_z+" "+v_x+" "+v_z+" got "+calc+" for "+px+"/"+pz);
            }
        }*/

    }

}
