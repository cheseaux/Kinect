package gl;

import java.awt.Color;
import java.awt.Rectangle;

import org.lwjgl.opengl.GL11;

public class GLPreviewBox extends Rectangle {
private static final long serialVersionUID = 0;
	
	public Color color;
	
	public int xSpeed;
	public int ySpeed;
	
	public GLPreviewBox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		this.color = new Color(1.0f, 1.0f, 1.0f);
		
		this.xSpeed = 0;
		this.ySpeed = 0;
	}
	
	public void update() {
		translate(xSpeed, ySpeed);
		
	}
	
	public void draw() {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3f(1,0,0); //face rouge
		GL11.glVertex3f(1,1,1);
		GL11.glVertex3f(1,1,-1);
		GL11.glVertex3f(-1,1,-1);
		GL11.glVertex3f(-1,1,1);
		GL11.glEnd();
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setSpeed(int x, int y) {
		this.xSpeed = x;
		this.ySpeed = y;
	}
}
