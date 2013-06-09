package gl;

import org.lwjgl.opengl.Display;

/**
 * Cette classe rassemble les constantes d'OpenGL
 * @author Jonathan Cheseaux et Willliam Trouleau
 *
 */
public class GLParameters {
	
	/** Largeur de la fenêtre */
	public static final int width = Display.getWidth();
	
	/** Hauteur de la fenêtre */
	public static final int height = Display.getHeight();
	
	/** Fréquence d'affichage */
	public static final int fps = 60;
}
