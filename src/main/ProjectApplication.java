package main;


import gl.GLParameters;
import gui.Splash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import kinect.KinectModule;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import text.TextDisplay;
import algorithms.PolygonPainter;
import demos.CamerasDemonstration;
import demos.DemoType;
import demos.Demonstrations;
import demos.KinectDemonstration;
import demos.Menu;
import demos.PhysicsDemonstration;
import demos.SlidesDemonstration;

/**
 * Application principale qui instancie et contrôle toutes les démonstrations
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class ProjectApplication implements Observer {
	
	/** Emplacement des slides */
	private static final String slideShowLocation = "data/slide/Diapositive";

	/** Menu */
	private Menu menu;
	
	/** Liste des démonstrations */
	private ArrayList<Demonstrations> demonstrations = new ArrayList<Demonstrations>();

	/** Texture d'arrière-plan */
	private Texture background;

	/**
	 * Initialise OpenGL et les démonstrations, lance le splashScreen de chargement 
	 * et charge les textures.
	 */
	public ProjectApplication() {
		initOpenGL();
		Splash splash = new Splash();
		splash.setProgress("OpenGL initialization ...", 1);
		splash.setProgress("Loading background texture ...", 2);

		try {
			background = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream("data/background_woodenwall.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		splash.setProgress("Loading kinect module ...", 3);
		KinectModule.getInstance();


		splash.setProgress("Loading demonstrations ...", 4);

		menu = new Menu();
		menu.addObserver(this);

		splash.setProgress("Loading slideshow ...", 5);
		SlidesDemonstration slides = new SlidesDemonstration(slideShowLocation, 22);
		slides.addObserver(menu);
		menu.addObserver(slides);
		demonstrations.add(slides);

		splash.setProgress("Loading physics engine ...", 6);
		PhysicsDemonstration physics1 = new PhysicsDemonstration(DemoType.PHYSICS1);
		physics1.addObserver(menu);
		menu.addObserver(physics1);
		demonstrations.add(physics1);

		splash.setProgress("Loading physics engine .....", 7);
		PhysicsDemonstration physics2 = new PhysicsDemonstration(DemoType.PHYSICS2);
		physics2.addObserver(menu);
		menu.addObserver(physics2);
		demonstrations.add(physics2);

		splash.setProgress("Loading physics engine .......", 8);
		PhysicsDemonstration physics3 = new PhysicsDemonstration(DemoType.PHYSICS3);
		demonstrations.add(physics3);
		menu.addObserver(physics3);
		physics3.addObserver(menu);

		splash.setProgress("Loading Kinect demonstration ...", 9);
		KinectDemonstration kinect = new KinectDemonstration(KinectDemonstration.DEPTH_TEXTURE);
		kinect.addObserver(menu);
		menu.addObserver(kinect);
		demonstrations.add(kinect);

		splash.setProgress("Loading Kinect cameras demonstration ...", 10);
		CamerasDemonstration camera = new CamerasDemonstration();
		camera.addObserver(menu);
		menu.addObserver(camera);
		demonstrations.add(camera);

		splash.setProgress("Loading Text Display...", 11);
		TextDisplay.println("");

		splash.setProgress("Loading complete", 12);
		splash.dispose();
		start();

		displayDemonstration(DemoType.SLIDES);

	}

	/**
	 * Initialise OpenGL
	 */
	public void initOpenGL() {
		try {
			
			//Décommenter pour mode FullScreen
//			Display.setFullscreen(true);
			Display.setDisplayMode(new DisplayMode(1080,750));
			Display.setVSyncEnabled(true);
			Display.setTitle("Kinect Project");
			Display.create();

		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		enable2DMode();
	}


	/**
	 * Construit les projections pour une vue en 2 dimensions (orthogonale)
	 */
	public void enable2DMode() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	/**
	 * OpenGL main loop
	 */
	public void start() {
		while (!Display.isCloseRequested()) {

			// Clear the screen and depth buffer
			float bckgrndCol = 0.2f;
			GL11.glClearColor(bckgrndCol, bckgrndCol, bckgrndCol, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();

			GLU.gluLookAt(0f, 0f, 0f, // where is the camera
					0f, 0f, -1f, // what point are we looking at
					0f, 1f, 0f); // which way is up

			drawBackground();

			try {
				for(Demonstrations demo : demonstrations) {
					demo.drawDemonstration();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			menu.draw();

			TextDisplay.draw();

			// Poll Input
			pollInput();

			Display.update();
			Display.sync(GLParameters.fps);

		}
		// Close application
		Display.destroy();
	}

	/**
	 * Affiche une démonstration
	 * @param type, le type de la démonstration à afficher
	 */
	public void displayDemonstration(DemoType type) {
		for (Demonstrations demo : demonstrations) {
			if (demo.getDemoType() == type) {
				demo.setVisible(true);
			} else {
				demo.setVisible(false);
			}
		}
	}

	/**
	 * Interception des pressions touche clavier
	 */
	public void pollInput() {
		if (!Keyboard.isCreated()) {
			return;
		}
		while (Keyboard.next()) {
			// Key pressed
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					displayDemonstration(DemoType.SLIDES);
					System.out.println("Slides !!!!!");
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
					menu.setVisible(true);
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
					displayDemonstration(DemoType.CAMERAS);
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
					displayDemonstration(DemoType.PHYSICS1);
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
					displayDemonstration(DemoType.PHYSICS2);
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
					displayDemonstration(DemoType.PHYSICS3);
				} 
				else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
					displayDemonstration(DemoType.KINECT);
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
					new PolygonPainter();
				}

				else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_R");
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_RIGHT");
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_LEFT");
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_SPACE");
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_A");
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
					for(Demonstrations demo : demonstrations) {
						demo.pollInput("KEY_S");
					}
				}


				else if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
					System.exit(0);
				}
			}
		}
	}

	/**
	 * Dessin de l'image d'arrière plan
	 */
	public void drawBackground() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 1, 0, 1, 10, -10);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		GL11.glColor4f(1,1,1,1);
		background.bind();
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, 9);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(0, 0, 0);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(1, 0, 0);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(1, 1, 0);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(0, 1, 0);
		GL11.glEnd();
		GL11.glPopMatrix();
	}


	public static void main(String[] args) {
		new ProjectApplication();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Menu && arg instanceof Integer) {
			Integer demoIndex = (Integer) arg;

			if (demoIndex >= DemoType.values().length) {
				return;
			}
			displayDemonstration(DemoType.values()[demoIndex]);
		}
	}

}
