package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
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

public class MainGameLoop {

    private static Terrain terrain;
    private static Terrain terrain2;
    private static Terrain terrain3;
    private static Terrain terrain4;

    public static void main(String[] args) {

        DisplayManager.createDisplay();

        Loader loader = new Loader();

        RawModel model = OBJLoader.loadObjModel("dragon", loader);

        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stall")));
        ModelTexture texture = staticModel.getTexture();
        texture.setShineDamper(10);
        texture.setReflectivity(1);

        Entity entity = new Entity(staticModel, new Vector3f(0, 0, -1), 0, 0, 0, 1);
        Light light = new Light(new Vector3f(2, 5, 5), new Vector3f(0.5f, 0.5f, 0.5f));
        List<Light> lights = new ArrayList<Light>();
        lights.add(light);
        // ********************* TERRAIN TEXTURE STUFF *********************
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);

        TerrainTexture blendTexture = new TerrainTexture(loader.loadTexture("blendMap"));
        // *****************************************************************

        terrain = new Terrain(-1, -1, loader, texturePack, blendTexture, "heightMap");
        terrain2 = new Terrain(0, -1, loader, texturePack, blendTexture, "heightMap");
        terrain3 = new Terrain(-1, 0, loader, texturePack, blendTexture, "heightMap");
        terrain4 = new Terrain(0, 0, loader, texturePack, blendTexture, "heightMap");

        // ------------------------------------------------------------------------
        List<Entity> entities = new ArrayList<Entity>();
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

        int range = 1000;
        int halfRange = range / 2;
        for (int i = 0; i < 3000; i++) {
            entities.add(genEntity(range, halfRange, random, grassTextured, terrain, 0.3f));

            if (i % 2 == 0)
                entities.add(genEntity(range, halfRange, random, fernTextured, terrain, 0.3f));

            if (i % 5 == 0) {
                entities.add(genEntity(range, halfRange, random, treeTextured, terrain, 2));

                entities.add(genEntity(range, halfRange, random, flowerTextured, terrain, 0.3f));

                if (i % 25 == 0)
                    entities.add(genEntity(range, halfRange, random, bunnyTextured, terrain, 0.3f));
            }
        }

        range = 1000;
        halfRange = range / 5;

        for (int i = 0; i < ShaderProgram.MAX_LIGHTS; i++) {
            float x = random.nextInt(range) - halfRange;
            float z = random.nextInt(range) - halfRange;
            
            lights.add(new Light(new Vector3f(random.nextInt(range) - halfRange,
                    pickTerrain(new Vector3f(x, 0, z)).getHeightOfTerrain(x, z) + random.nextFloat() + 10 + 0.1f, random.nextInt(range) - halfRange),

                    new Vector3f(x, random.nextInt(255) / 255.0f,
                            z),
                    new Vector3f(1f, 0.1f, 0.04f)));
        }
        // ------------------------------------------------------------------------

        Player player = new Player(bunnyTextured, new Vector3f(0, 0, 0), 0, 0, 0, 1);
        Camera camera = new Camera(player);
        camera.position = new Vector3f(0, 5, 15);

        MasterRenderer renderer = new MasterRenderer(loader);

        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("socuwan"), new Vector2f(0.5f, 0.5f),
                new Vector2f(0.25f, 0.25f));
        // guis.add(gui);
        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while (!Display.isCloseRequested()) {
            player.move(pickTerrain(player.getPosition()));

            camera.move();

            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            renderer.processTerrain(terrain2);
            renderer.processTerrain(terrain3);
            renderer.processTerrain(terrain4);
            renderer.processEntity(entity);

            for (Entity ent : entities) {
                renderer.processEntity(ent);
            }

            renderer.render(lights, camera);
            guiRenderer.render(guis);

            DisplayManager.updateDisplay();
        }
        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

    private static Entity genEntity(int range, int halfRange, Random random, TexturedModel model, Terrain terrain,
            float baseScale) {
        float x = random.nextInt(range) - halfRange;
        float z = random.nextInt(range) - halfRange;

        float y = pickTerrain(new Vector3f(x, 0, z)).getHeightOfTerrain(x, z);

        return new Entity(model, random.nextInt((int) Math.pow(model.getTexture().getNumberOfRows(), 2)),
                new Vector3f(x, y, z), 0, random.nextInt(360), 0, random.nextFloat() + baseScale);
    }

    private static Terrain pickTerrain(Vector3f position) {
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
