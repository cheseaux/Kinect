package demos;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Elément du menu
 * @see Menu
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class MenuItem {

	/** Texture du logo */
	private Texture logo;
	
	/** RGB color */
	private float r;
	private float g;
	private float b;

	public MenuItem(String logoLocation) {
		r = 1.0f;
		g = 1.0f;
		b = 1.0f;

		try {
			logo = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(logoLocation));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void setColor(float r, float g, float b){
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	/** Dessin du logo */
	public void draw() {
		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		logo.bind();
		
		
		GL11.glColor4f(r, g, b, 1f);
		
		GL11.glPushMatrix();

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

		GL11.glPopMatrix();
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
//		TextureImpl.bindNone();
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	public void drawReflection() {
		float d = -0.02f;
		
		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		logo.bind();
		
		
		GL11.glPushMatrix();

		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glColor4f(r, g, b, 0.4f);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(-1+d, -1, 0);
		GL11.glColor4f(r, g, b, 0.4f);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(1+d, -1, 0);
		GL11.glColor4f(r, g, b, 0.0f);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(1, 1, 0);
		GL11.glColor4f(r, g, b, 0.0f);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(-1, 1, 0);
		GL11.glEnd();

		GL11.glPopMatrix();
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
//		TextureImpl.bindNone();
		GL11.glDisable(GL11.GL_BLEND);
	}
	
}
