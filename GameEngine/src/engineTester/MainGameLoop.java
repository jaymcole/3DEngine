package engineTester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.Particle;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import shaders.ShaderProgram;
import terrain.Terrain;
import terrain.Terrain_OLD;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {

    private static Terrain terrain;
    private static Terrain terrain2;
    private static Terrain terrain3;
    private static Terrain terrain4;

    private static final float WATER_HEIGHT = 0;
    public static final float TERRAIN_SIZE = 2048;
    public static final float TERRAIN_MAX_HEIGHT = 100;
    public static final float WATER_TILE_SIZE = TERRAIN_SIZE / 2;

    public static void main(String[] args) {

        DisplayManager.createDisplay();
        Loader loader = new Loader();
        TextMaster.init(loader);
        
        Random random = new Random();
        ModelData data = OBJFileLoader.loadOBJ("fern");
        RawModel fernModel = loader.loadVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
                data.getIndices());
        TexturedModel fernTextured = new TexturedModel(fernModel, new ModelTexture(loader.loadTexture("fernAtlas")));
        fernTextured.getTexture().setHasTransparency(true);
        fernTextured.getTexture().setNumberOfRows(2);

        data = OBJFileLoader.loadOBJ("grass");
        RawModel grassModel = loader.loadVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
                data.getIndices());
        TexturedModel grassTextured = new TexturedModel(grassModel,
                new ModelTexture(loader.loadTexture("grassTexture")));
        grassTextured.getTexture().setHasTransparency(true);
        grassTextured.getTexture().setUseFakeLighting(true);

        TexturedModel flowerTextured = new TexturedModel(grassModel, new ModelTexture(loader.loadTexture("flower")));
        flowerTextured.getTexture().setHasTransparency(true);
        flowerTextured.getTexture().setUseFakeLighting(true);

        data = OBJFileLoader.loadOBJ("bunny");
        RawModel bunnyModel = loader.loadVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
                data.getIndices());
        TexturedModel bunnyTextured = new TexturedModel(bunnyModel, new ModelTexture(loader.loadTexture("white")));

        data = OBJFileLoader.loadOBJ("pineTree");
        RawModel treeModel = loader.loadVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
                data.getIndices());
        TexturedModel treeTextured = new TexturedModel(treeModel, new ModelTexture(loader.loadTexture("pineTree")));

        data = OBJFileLoader.loadOBJ("dock");
        RawModel dockModel = loader.loadVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
                data.getIndices());
        TexturedModel dockTextured = new TexturedModel(dockModel, new ModelTexture(loader.loadTexture("white")));
        
        
        
        
        ArrayList<Entity> entities = new ArrayList<Entity>();
        ArrayList<Entity> normalMapEntities = new ArrayList<Entity>();
        Player player = new Player(bunnyTextured, new Vector3f(TERRAIN_SIZE / 2, 0, TERRAIN_SIZE / 2), 0, 0, 0, 1);
        entities.add(player);

        Camera camera = new Camera(player);
        camera.position = new Vector3f(0, 5, 15);
        
        MasterRenderer renderer = new MasterRenderer(loader, camera);
        ParticleMaster.init(loader, renderer.getProjectionMatrix());
        
        FontType font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
        GUIText text = new GUIText("Something something something", 3, font, new Vector2f(0.5f,0.5f), 0.5f, true);
        text.setColour(0, 0, 0);
        
        RawModel model = OBJLoader.loadObjModel("dragon", loader);

        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stall")));
        ModelTexture texture = staticModel.getTexture();
        texture.setShineDamper(10);
        texture.setReflectivity(1);
        Entity entity = new Entity(staticModel, new Vector3f(0, 0, -1), 0, 0, 0, 1);
        entities.add(entity);

        List<Light> lights = new ArrayList<Light>();
        Light sun = new Light(new Vector3f(2 * 100000, 5 * 10000, 5 * -10000), new Vector3f(0.9f, 0.9f, 0.9f));
        lights.add(sun);
        // ********************* TERRAIN TEXTURE STUFF *********************
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("sand"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassy2"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);

        TerrainTexture blendTexture = new TerrainTexture(loader.loadTexture("niceheight_blend"));
        // *****************************************************************

        List<Terrain> terrains = new ArrayList<Terrain>();
        terrain = new Terrain(0, 0, loader, texturePack, blendTexture, "niceheight");
        terrains.add(terrain);

        // ------------------------------------------------------------------------
        
        
        
        
        int range = (int) (TERRAIN_SIZE);
        int halfRange = range / 2;
        for (int i = 0; i < 2000; i++) {
            genEntity(range, halfRange, random, grassTextured, terrain, 0.3f, entities);

            if (i % 2 == 0)
                genEntity(range, halfRange, random, fernTextured, terrain, 0.3f, entities);

            if (i % 5 == 0) {
                genEntity(range, halfRange, random, treeTextured, terrain, 2, entities);

                genEntity(range, halfRange, random, flowerTextured, terrain, 0.3f, entities);

                if (i % 25 == 0)
                    genEntity(range, halfRange, random, bunnyTextured, terrain, 0.3f, entities);
            }
        }

        Entity dock = new Entity(dockTextured, new Vector3f(989, -125, 775), 0, 120, 0, 2);
        entities.add(dock);
        
        for (int i = 0; i < ShaderProgram.MAX_LIGHTS; i++) {
            float x = random.nextInt(range) - halfRange;
            float z = random.nextInt(range) - halfRange;

            lights.add(new Light(new Vector3f(random.nextInt(range) - halfRange,
                    pickTerrain(new Vector3f(x, 0, z)).getHeightOfTerrain(x, z) + random.nextFloat() + 10 + 0.1f,
                    random.nextInt(range) - halfRange),

                    new Vector3f(x, random.nextInt(255) / 255.0f, z), new Vector3f(1f, 0.1f, 0.04f)));
        }

        
        
        TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel")));
        for(int i = 0; i < 10; i++) {
            Entity barrel = new Entity(barrelModel, new Vector3f (random.nextInt(range), 2, random.nextInt(range)), 0,0,0,1f);
            barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
            barrelModel.getTexture().setShineDamper(10);
            barrelModel.getTexture().setReflectivity(0.5f);
            normalMapEntities.add(barrel);            
        }
        Entity barrel = new Entity(barrelModel, new Vector3f (halfRange, 2, halfRange), 0,0,0,1f);
        barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
        barrelModel.getTexture().setShineDamper(10);
        barrelModel.getTexture().setReflectivity(0.5f);
        normalMapEntities.add(barrel);
        
        // ------------------------------------------------------------------------

        

        

        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

        // *********************************Water Renderer
        // Set-up*********************************************************
        WaterFrameBuffers buffers = new WaterFrameBuffers();
        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
        List<WaterTile> waters = new ArrayList<WaterTile>();
        waters.add(new WaterTile(TERRAIN_SIZE / 2, TERRAIN_SIZE / 2, WATER_HEIGHT));
        // waters.add(new WaterTile(0,0, 1f));

        // ***************************************************************************************************************

        // *********************************GUI
        // Set-up*******************************************************************
        List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
        // GuiTexture gui = new GuiTexture(loader.loadTexture("socuwan"), new
        // Vector2f(0.5f, 0.5f),
        // new Vector2f(0.25f, 0.25f));
        // GuiTexture refraction = new GuiTexture
        // (buffers.getRefractionTexture(), new Vector2f(0.5f, 0.5f), new
        // Vector2f(0.25f, 0.25f));
        // GuiTexture reflection = new GuiTexture
        // (buffers.getReflectionTexture(), new Vector2f(-0.5f, 0.5f), new
        // Vector2f(0.25f, 0.25f));
        // guiTextures.add(refraction);
        // guiTextures.add(reflection);
        GuiTexture shadowMap = new GuiTexture(renderer.getShadowMapTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f));
//        guiTextures.add(shadowMap);
        GuiRenderer guiRenderer = new GuiRenderer(loader);
        // ***************************************************************************************************************

        ParticleTexture smokeParticles = new ParticleTexture(loader.loadTexture("smoke"), 8, true);
        ParticleTexture fuctParticles = new ParticleTexture(loader.loadTexture("fuck"), 1, true);
        
        ParticleSystem system_smoke = new ParticleSystem(smokeParticles, 100, 10f, -0.2f, 10f, 50f);
//        system.setDirection(new Vector3f(1,0,1), 0.1f);
        system_smoke.setLifeError(0.1f);
        system_smoke.setSpeedError(0.4f);
        system_smoke.setScaleError(0.8f);
        system_smoke.randomizeRotation();
        
        ParticleSystem system_fuck = new ParticleSystem(fuctParticles, 1f, 200f, 0f, 10f, 50f);
//      system.setDirection(new Vector3f(1,0,1), 0.1f);
        system_fuck.setLifeError(0.1f);
        system_fuck.setSpeedError(0.4f);
        system_fuck.setScaleError(0.8f);
        system_fuck.randomizeRotation();
        
        
        Vector3f steamPosition = new Vector3f(490, WATER_HEIGHT - 2, 747);
        Vector3f fuckPosition = new Vector3f(random.nextInt((int)TERRAIN_SIZE), 200, random.nextInt((int)TERRAIN_SIZE));
        while (!Display.isCloseRequested()) {
            player.move(pickTerrain(player.getPosition()));
            camera.move();
            picker.update();

            system_fuck.generateParticles(fuckPosition);
            fuckPosition.x = random.nextInt((int)TERRAIN_SIZE);
            fuckPosition.z = random.nextInt((int)TERRAIN_SIZE);
            system_smoke.generateParticles(steamPosition);
            ParticleMaster.update(camera);
            
            for(Entity e : normalMapEntities) {
                e.increaseRotation(0, 0.1f, 0);
            }
            
            
            if (Keyboard.isKeyDown(Keyboard.KEY_Q)) { 
                System.out.println(player.getPosition().toString() + "     " + player.getRotx() + ", "+ player.getRoty() + ", "+ player.getRotz() + ", ");
            }
            
            renderer.renderShowMap(entities, sun);
            
            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
            // Render reflection texture
            buffers.bindReflectionFrameBuffer();
            float distance = 2 * (camera.getPosition().y - waters.get(0).getHeight());
            camera.getPosition().y -= distance;
            camera.invertPitch();
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -waters.get(0).getHeight()));
            camera.getPosition().y += distance;
            camera.invertPitch();

            // Render refraction texture
            buffers.bindRefractionFrameBuffer();
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, waters.get(0).getHeight()));

            buffers.unbindCurrentFrameBuffer();

            // Render final scene
            GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, 100000));
            waterRenderer.render(waters, camera, sun);
//            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_LINE_SMOOTH_HINT);
//            GL11.glEnable(GL11.GL_LINE_SMOOTH);
//            Gl11.glHint(GL_LINE_SMOOTH_HINT,GL_NICEST);
//            glEnable(GL_LINE_SMOOTH);
//            GL11.glLineWidth(1f);
//            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            ParticleMaster.renderParticles(camera);
//            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            
            guiRenderer.render(guiTextures);
            TextMaster.render();
            DisplayManager.updateDisplay();
        }
        
        ParticleMaster.cleanUp();
        TextMaster.cleanUp();
        buffers.cleanUp();
        waterShader.cleanUp();
        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

    private static void genEntity(int range, int halfRange, Random random, TexturedModel model, Terrain terrain,
            float baseScale, ArrayList<Entity> entities) {
        float x = random.nextInt(range) - halfRange + (TERRAIN_SIZE / 2);
        float z = random.nextInt(range) - halfRange + (TERRAIN_SIZE / 2);

        float y = pickTerrain(new Vector3f(x, 0, z)).getHeightOfTerrain(x, z);

        if (y > WATER_HEIGHT) {
            entities.add(new Entity(model, random.nextInt((int) Math.pow(model.getTexture().getNumberOfRows(), 2)),
                    new Vector3f(x, y, z), 0, random.nextInt(360), 0, random.nextFloat() + baseScale));
        } else {
            genEntity(range, halfRange, random, model, terrain, baseScale, entities);
        }
    }

    private static Terrain pickTerrain(Vector3f position) {
        if(true)
        return terrain;
        if (position.x >= 0 && position.z >= 0) {
            return terrain4;
        } else if (position.x < 0 && position.z >= 0) {
            return terrain3;
        } else if (position.x >= 0 && position.z < 0) {
            return terrain2;
        } else {
            return terrain;
        }
    }

}
