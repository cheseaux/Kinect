package gl;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import utils.GLParameters;

public class GLviewer {
	private static int width = GLParameters.width;
	private static int height = GLParameters.height;
	
	private static int fps = 60;
	private static float current_time;
	
	private static int selectedPolygonIndex = -1;
	private static Color selectedColor = new Color(0.2f, 0.2f, 0.7f);
	private static Color normalColor = new Color(0.8f, 0.8f, 0.8f);
	
	private ArrayList<GLPreviewBox> components = new ArrayList<GLPreviewBox>();
	private static int radius = 1000;
	
	public GLviewer(){
		
		initOpenGL();
		initScene();
	}
	
	private void initScene() {
		for (int i = 0; i < 3; i++) {
			GLPreviewBox comp = new GLPreviewBox(i*120, i*100, 100, 80);
			comp.setSpeed(-1, 0);
			components.add(comp);
		}
	}
	
	private void initOpenGL() {
		try {
			Display.setDisplayMode(new DisplayMode(width,height));
			Display.setTitle("Minority Report Viewer");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// init OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(100.0f, 640.0f/480.0f, 1.0f, 1000.0f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		System.out.println("OpenGL initiated");
	}
	
	public void start() {

		while (!Display.isCloseRequested()) {
			current_time = System.currentTimeMillis();
			
			GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

			GL11.glMatrixMode( GL11.GL_MODELVIEW );
			GL11.glLoadIdentity( );
			GLU.gluLookAt(0,-20,0,0,0,0,0,0,1);

			pollInput();
			updateScene();
			renderScene();
			
			GL11.glFlush();
			Display.update();
			Display.sync(fps);
		}


		Display.destroy();
	}
	
	private void updateScene(){
		for (GLPreviewBox box : components) {
			box.update();
		}
	}
	
	private void renderScene() {
		for (GLPreviewBox box : components) {
			box.draw();
		}
	}
	
	private void pollInput(){
		try {
			Mouse.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Mouse.isButtonDown(0) && selectedPolygonIndex == -1) {
			int x = Mouse.getX();
			int y = Mouse.getY();
			
			for (int i = 0; i < components.size(); i++) {
				if(components.get(i).contains(x, y)) {
					selectedPolygonIndex = i;
					components.get(i).setColor(selectedColor);
					components.get(i).setSpeed(0, 0);
				}
			}
		}
		if (!Mouse.isButtonDown(0) && selectedPolygonIndex != -1) {
			components.get(selectedPolygonIndex).setColor(normalColor);
			selectedPolygonIndex = -1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			
		}
	}
	
	public static void main(String[] argv) {
		GLviewer viewer = new GLviewer();
		viewer.start();
	}
}
