package swen30006.driving;

import java.lang.Math;
import java.util.Properties;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;

import world.World;

/**
 * This class provides functionality for use within the simulation system. It is NOT intended to be
 * read or understood for SWEN30006 Part C. The lack of comments is intended to reinforce this.
 * We take no responsibility if you use time unproductively trying to understand this code.
 */
public class Simulation extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture img;
	public static TiledMap map;
	public static long startTime;
	public static OrthographicCamera camera;
	private World world;
	OrthogonalTiledMapRenderer tiledMapRenderer;
	private enum CameraMode {WORLD, PLAYER};
	private static CameraMode CAMERA_MODE = CameraMode.WORLD;
	private static final int PLAYER_VIEW = 11;
	private static boolean gameEnded = false;
	private static boolean gameWon = false;
	public static boolean DEBUG_MODE = true;
	private BitmapFont font;
	
	private static float TIME_STEP = 1/45f;
	
	public Simulation(String[] arg) {
		super();
	}
	
	@Override
	public void create () {
		/* From new template 1/5/2018
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		*/
		startTime = System.currentTimeMillis();
		
		Properties drivingProperties = new Properties();
		// Defaults
		drivingProperties.setProperty("Map", "lecture-preview.tmx");
		drivingProperties.setProperty("Number_of_Rounds", "20");
		
		try (FileReader inStream = new FileReader("assets/Driving.Properties")) {
			drivingProperties.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} 
		// int numberOfPlayers = Integer.parseInt(drivingProperties.getProperty("Number_of_Players"));
		
		// int numberOfRounds = Integer.parseInt(drivingProperties.getProperty("Number_of_Rounds"));
		String mapName = drivingProperties.getProperty("Map");
		map = new TmxMapLoader().load(mapName);
		
		String controllerName = drivingProperties.getProperty("Controller");
		
		// Create the world
		world = new World(map, controllerName);
		
		// Set the camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false,World.MAP_WIDTH,World.MAP_HEIGHT);
		camera.update();
		
		// Define scale per unit
		float unitScale = 1 / 32f;
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map,unitScale);
		Gdx.input.setInputProcessor(this);
		
		// Initialize fonts
		font = new BitmapFont();
	}

	private float accumulator = 0;
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		
		tiledMapRenderer.getBatch().begin();
		world.render(tiledMapRenderer.getBatch());
		tiledMapRenderer.getBatch().end();
		
		float frameTime = Math.min(Gdx.graphics.getDeltaTime(), 0.25f);

	    accumulator += frameTime;
	    while (accumulator >= TIME_STEP) {
	        accumulator -= TIME_STEP;
	        world.update(TIME_STEP);
	    }
		
		if(CAMERA_MODE.equals(CameraMode.PLAYER)){
			followCar();
		}
		
		camera.update();
		batch = new SpriteBatch();
		batch.begin();
		
		String key = "K"+Integer.toString(world.getCar().getKey());
		String health = "H"+Integer.toString(Math.round(world.getCar().getHealth()));
		String status = key+"/"+health;
		font.getData().setScale(1.5f);
		int offset = 1;
		//Relative to screen size.
		font.draw(batch, status, World.MAP_PIXEL_SIZE, Gdx.graphics.getHeight() - offset*World.MAP_PIXEL_SIZE);
		font.setColor(Color.GREEN);
		
		//If we win or lose!
		if(gameEnded){
			font.getData().setScale(5f);
			String winText = gameWon ? "You WIN!" : "You LOSE!";
			font.setColor(gameWon ? Color.GREEN : Color.RED);
			final GlyphLayout layout = new GlyphLayout(font, winText);

			final float fontX = 0 + (Gdx.graphics.getWidth() - layout.width) / 2;
			final float fontY = 0 + (Gdx.graphics.getHeight() + layout.height) / 2;

			font.draw(batch, layout, fontX, fontY);
			String timeText = gameWon ? "You escaped and it took: " : "You failed and it took: ";
			System.out.println(timeText + ((System.currentTimeMillis() - startTime) / 1000+" seconds!"));
			batch.end();
			/*
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			*/
			Gdx.app.exit();
		} else {
			batch.end();
		}
	}
	
	@Override
	public void dispose () {
		/* From new template 1/5/2018
		batch.dispose();
		img.dispose();
		*/
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		// Zoom in: 11x11 grid centered on the player
		if(keycode == Input.Keys.X){
			camera.viewportWidth = PLAYER_VIEW;
			camera.viewportHeight = PLAYER_VIEW;
			followCar();
			CAMERA_MODE = CameraMode.PLAYER;
		}
		if(keycode == Input.Keys.Z){
			
			camera.viewportWidth = World.MAP_WIDTH;
			camera.viewportHeight = World.MAP_HEIGHT;
			camera.position.set(0,0,0);
			CAMERA_MODE = CameraMode.WORLD;
		}
		if(keycode == Input.Keys.F){
			DEBUG_MODE = true;
		}
		camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100/camera.viewportWidth);

		float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
		float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

		camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
		camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	public void followCar(){
		
		float carXPosition = world.getCar().getX();
		float carYPosition = world.getCar().getY();
		
		camera.position.set(carXPosition, carYPosition, 0);
	}
	
	public static void endGame(boolean won){
		gameEnded = true;
		gameWon = won;
	}

}
