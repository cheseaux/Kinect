package demos;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 * This class implements a graphical Slide
 * which is used for the presentation
 * @author Jonathan Cheseaux & William Trouleau
 *
 */
public class Slide {

	/** Slide texture */
	private Texture texture;
	
	/**
	 * Construit une nouvelle instance de l'objet Slide 
	 * @param textureLocation le chemin d'accès à la texture
	 * @param useTexture true, si on veut utiliser une texture
	 */
	public Slide(String textureLocation, boolean useTexture) {
		super();

		// load texture from PNG file
		try {
			texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(textureLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Permet de dessiner les slides avec un effet 3D
	 * @param n, le nombre de slides
	 */
	public void drawPartialCylinder(int n){
		texture.bind();
		
		double a = Math.PI/4;
		double step = (Math.PI/2) / n;

		double y0 = -Math.sqrt(2)/2;
		double y1 = Math.sqrt(2)/2;

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		for (int i = 0; i <= n; i++) {
			double x = Math.cos(a);
			double z = -Math.sin(a);

			GL11.glNormal3d(x, 0.0, z);
			GL11.glTexCoord2d(1-(double)i/n, 1);
			GL11.glVertex3d(x, y0, z);

			GL11.glNormal3d(x, 0.0, z);
			GL11.glTexCoord2d(1-(double)i/n, 0);
			GL11.glVertex3d(x, y1, z);

			a += step;
		}
		GL11.glEnd();
	}

	/**
	 * Just draw an openGL quad
	 */
	public void drawQuad() {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(-1, -1, 0);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(1, -1, 0);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(1, 1, 0);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(-1, 1, 0);
		GL11.glEnd();
	}


}