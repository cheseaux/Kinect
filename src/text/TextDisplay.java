package text;

import java.awt.Font;
import java.util.LinkedList;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
 
/**
 * Cette classe permet d'afficher des informations de Debug à l'écran
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class TextDisplay {
	
	private String debug = "";
 
	// Text position
	private static float x = 10;
	private static float y = 30;
	
	// The fonts to draw to the screen
	private static int fontSize = 28;
	private static Color fontColor = Color.green;
	private static UnicodeFont font;
	
	
	// Display duration in seconds
	private static long duration = 8;
	
	// Strings to be displayed and the time the have been commanded to be displayed
	private static LinkedList<String> lines = new LinkedList<String>();
	private static LinkedList<Long> printTime = new LinkedList<Long>();
	
	static {
		init();
	}
	
	private TextDisplay(){}
	
	
	@SuppressWarnings("unchecked")
	public static void init() {
		Font awtFont = new Font("Monaco", java.awt.Font.BOLD, fontSize);
		font = new UnicodeFont(awtFont);
		font.getEffects().add(new ColorEffect(java.awt.Color.white));
		font.addAsciiGlyphs();
		try {
			font.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
 
	
	/**
	 * Affiche les infos à l'écran
	 * @param string
	 */
	public static void println(String string) {
		//Décommenter pour activer le mode debug
//		return;
//		if (lines.size() == maxLines) {
//			printTime.remove();
//			lines.remove();
//		}
//		
//		lines.add(string);
//		printTime.add(System.currentTimeMillis());
	}
	
	
	/**
	 * Set Text projection:
	 * 
	 * Orthogonal Projection [0, Display.width] x [Display.height, 0] 
	 * Note that the veritcal axis is inversed to Display the text in the right direction, 
	 * but we draw as if it was not.
	 */
	private static void setProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	
	
	/**
	 * render text
	 */
	public static void draw() {
		setProjection();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);        
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float xtrans = 0;
		for (int i = 0; i < lines.size(); i++) {
			if (System.currentTimeMillis() - printTime.get(i) >  duration*1000 
					&& !lines.isEmpty() && !printTime.isEmpty()) {
				lines.remove();
				printTime.remove();
			} else {
				font.drawString(x,Display.getHeight()- y - xtrans, lines.get(i), fontColor);
				xtrans += fontSize;
			}
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
 
	

	
	
	public void start() {
		initGL(800,600);
		init();
 
		while (true) {
			float bckgrndCol = 0.6f;
			GL11.glClearColor(bckgrndCol, bckgrndCol, bckgrndCol, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			draw();
			pollInput();
			
			Display.update();
			Display.sync(60);
 
			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
 
	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width,height));
			Display.create();
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
 
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);        
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);                    
 
		GL11.glClearColor(0.6f, 0.6f, 0.6f, 1.0f);                
        GL11.glClearDepth(1);                                       
 
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
        GL11.glViewport(0,0,width,height);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
	}
 
	/**
	 * Capte les pressions de touche clavier
	 */
	public void pollInput() {

		while (Keyboard.next()) {
			// Key pressed
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
					println(debug);
					debug = "";
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) { 
					debug = debug + " ";
				} else {
					debug = debug + Keyboard.getKeyName(Keyboard.getEventKey());
				}
			}
		}
	}
	
	
	public static void main(String[] argv) {
		TextDisplay fontExample = new TextDisplay();
		fontExample.start();
	}
	
	
}

