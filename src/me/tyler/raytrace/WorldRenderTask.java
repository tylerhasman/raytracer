package me.tyler.raytrace;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
        for(float i = sectionX; i < section.getWidth() + sectionX;i++){

            float rayScreenX = 0.5f - i / totalWidth;

            float rayScreenXFov = (rayScreenX * fov);

            float rayVelX = FastMath.cos((float) Math.toRadians((rayScreenXFov + cameraYaw)));
            float rayVelZ = FastMath.sin((float) Math.toRadians((rayScreenXFov + cameraYaw)));

            float rayWorldX = (rayScreenX / fov) + cameraX;
            float rayWorldZ = (rayScreenX / fov) + cameraZ;

            for(float j = sectionY; j < section.getHeight() + sectionY;j++){

                float rayScreenY = 0.5f - j / totalHeight;

                float rayWorldY = (rayScreenY / fov) + cameraY;

                float rayVelY = FastMath.sin((float) Math.toRadians((rayScreenY * 45f + cameraPitch)));

                Tile seenTile = null;
                float hitRayProjX = 0.5f;
                float hitRayProjY = 0.5f;
                WorldObject seenObject = null;

                final float accuracy = 50f;

                float k;

                float lightR = 0, lightG = 0, lightB = 0;

                for(k = accuracy / 2f; k < drawDistance * accuracy;k++){

                    float checkX = rayWorldX + rayVelX * k / accuracy;
                    float checkY = rayWorldY + rayVelY * k / accuracy;
                    float checkZ = rayWorldZ + rayVelZ * k / accuracy;

                    for(Light light : world.getLights()){
                        float dst = light.dst(checkX, checkZ);
                        if(dst <= light.size){
                            lightR += (light.r * light.intensity * (1f - dst / light.size)) / accuracy;
                            lightG += (light.g * light.intensity * (1f - dst / light.size)) / accuracy;
                            lightB += (light.b * light.intensity * (1f - dst / light.size)) / accuracy;
                        }
                    }

                    int tile = world.getTileId((int) checkX, (int) checkZ);

                    if(checkY > 1F){
                        if(rayWorldY < 1F)
                            tile = Tile.SKY.getId();
                        else
                            continue;
                    }else if(checkY < 0F)
                        if(rayWorldY > 0F)
                            tile = Tile.GROUND.getId();
                        else
                            continue;

                    for(WorldObject worldObject : world.getWorldObjects()){
                        if(worldObject.intersects(checkX, checkY, checkZ)){
                            seenObject = worldObject;
                            break;
                        }
                    }

                    if(seenObject != null)
                        break;

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

                float dstX = rayVelX * k / accuracy;
                float dstZ = rayVelZ * k / accuracy;

                float dst = (float) (1f - Math.min(1f, Math.sqrt(dstX * dstX + dstZ * dstZ) / drawDistance)) / 2f;

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

}
