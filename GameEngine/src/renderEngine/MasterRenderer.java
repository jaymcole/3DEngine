package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import shaders.StaticShader;
import shaders.TerrainShader;
import shadows.ShadowMapMasterRenderer;
import skybox.SkyboxRenderer;
import terrain.Terrain;
import terrain.Terrain_OLD;

public class MasterRenderer {

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 5000 ;

    public static final float RED = 0.5444f;//126 / 255.0f;
    public static final float GREEN = 0.62f;//192 / 255.0f;
    public static final float BLUE = 0.69f;//238 / 255.0f;
    
    private Matrix4f projectionMatrix;
    
    private StaticShader shader = new StaticShader();
    private EntityRenderer renderer;

    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader = new TerrainShader();
    
    
    private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
    private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<TexturedModel, List<Entity>>();
    private List<Terrain> terrains = new ArrayList<Terrain>();

    private NormalMappingRenderer normalMapRenderer;
    
    private SkyboxRenderer skyboxRenderer;
    
    private ShadowMapMasterRenderer shadowMapRenderer;
    
    public MasterRenderer(Loader loader, Camera camera) {
        
        createProjectionMatrix();
        renderer = new EntityRenderer(shader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
        normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
        this.shadowMapRenderer = new ShadowMapMasterRenderer(camera);
    }
    
    public Matrix4f getProjectionMatrix () {
        return projectionMatrix;
    }
    
    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }
    
    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
    
    public void renderScene(List<Entity> entities, List<Entity> normalMapEntities, List<Terrain> terrains, List<Light> lights, Camera camera, Vector4f clipPlane) {
        for(Terrain terrain : terrains) {
            processTerrain(terrain);
        }
        for(Entity entity : entities) {
            processEntity(entity);
        }
        for(Entity entity : normalMapEntities) {
            processNormalMapEntity(entity);
        }
        
        render (lights, camera, clipPlane);
        
    }
    
    public void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
        prepare();
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        skyboxRenderer.render(camera, RED, GREEN, BLUE);

        shader.start();
        shader.loadClipPlane(clipPlane);
        shader.loadSkyColor(RED, GREEN, BLUE);
        shader.loadLights(lights);
        shader.loadViewMatrix(camera);
        renderer.render(entities);
        shader.stop();
        
        normalMapRenderer.render(normalMapEntities, clipPlane, lights, camera);
        
        terrainShader.start();
        terrainShader.loadClipPlane(clipPlane);
        terrainShader.loadSkyColor(RED, GREEN, BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains, shadowMapRenderer.getToShadowMapSpaceMatrix());
        terrainShader.stop();
        
        
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        terrains.clear();
        entities.clear();
        normalMapEntities.clear();
    }

    /**
     * Called every frame - prepares OpenGL to render the game
     */
    public void prepare() {
        
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(RED, GREEN, BLUE, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL13.glActiveTexture(GL13.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture());
    }

//    private void createProjectionMatrix() {
//        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
//        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
//        float x_scale = y_scale / aspectRatio;
//        float frustum_length = FAR_PLANE - NEAR_PLANE;
//
//        projectionMatrix = new Matrix4f();
//        projectionMatrix.m00 = x_scale;
//        projectionMatrix.m11 = y_scale;
//        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
//        projectionMatrix.m23 = -1;
//        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
//        projectionMatrix.m33 = 0;
//    }
    
    private void createProjectionMatrix(){
        projectionMatrix = new Matrix4f();
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }
    
    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);

        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }
    
    public void processNormalMapEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = normalMapEntities.get(entityModel);

        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            normalMapEntities.put(entityModel, newBatch);
        }
    }
    
    public void renderShowMap(List<Entity> entityList, Light sun) {
        for (Entity entity : entityList) {
            processEntity(entity);
        }
        shadowMapRenderer.render(entities, sun);
        entities.clear();
    }
    
    public int getShadowMapTexture() {
        return shadowMapRenderer.getShadowMap();
    }

    public void cleanUp() {
        shader.cleanUp();
        terrainShader.cleanUp();
        normalMapRenderer.cleanUp();
        shadowMapRenderer.cleanUp();
    }

}