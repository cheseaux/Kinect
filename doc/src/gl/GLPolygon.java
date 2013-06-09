package gl;

import java.awt.Color;
import java.awt.Polygon;

import org.lwjgl.opengl.GL11;

import utils.GLParameters;

public class GLPolygon extends Polygon {
	
	private static final long serialVersionUID = 0;
	
	public Color color;
	
	public int xSpeed;
	public int ySpeed;
	
	public GLPolygon(Color color) {
		this.color = color;
		
		this.xSpeed = 0;
		this.ySpeed = 0;
	}
	
	public void update() {
		translate(xSpeed, ySpeed);
		
		for (int i = 0; i < xpoints.length; i++) {
			if (xpoints[i] < -100 || xpoints[i] > GLParameters.width + 100) {
				translate(GLParameters.width + 100, 0);
			}
		}
	}
	
	public void draw() {
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor3f((float)color.getRed()/255, (float)color.getGreen()/255, (float)color.getBlue()/255);
		for (int i = 0; i < xpoints.length; i++) {
			GL11.glVertex2f(xpoints[i], ypoints[i]);
		}
		GL11.glEnd();
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setSpeed(int x, int y) {
		this.xSpeed = x;
		this.ySpeed = y;
	}
	
	public void MoveCenterToPosition(int x, int y){
		this.translate(x-(xpoints[0]+xpoints[1])/2, y-(ypoints[0]+ypoints[2])/2);
	}
	
}
