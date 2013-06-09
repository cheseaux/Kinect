package algorithms;

import org.jbox2d.common.Vec2;

import box2d.PhysicsWorld;

/** 
 * Based on JSFL util by mayobutter (Box2D Forums)
 * and  Eric Jordan (http://www.ewjordan.com/earClip/) Ear Clipping experiment in Processing
 * 
 * Tarwin Stroh-Spijer - Touch My Pixel - http://www.touchmypixel.com/
 * 
 * Traduction d'un code ActionScript3 en java
 * */
public class Triangle {
	
	public int[] x = new int[3];
	public int[] y = new int[3];

	
	/**
	 * Construit un nouveau triangle au moyen de 3 points
	 * @param x1, abscisse premier point
	 * @param y1, ordonnée premier point
	 * @param x2, abscisse deuxième point
	 * @param y2, ordonnée deuxième point
	 * @param x3, abscisse troisième point
	 * @param y3, ordonnée troisième point
	 */
	public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {

		int dx1 = x2-x1;
		int dx2 = x3-x1;
		int dy1 = y2-y1;
		int dy2 = y3-y1;
		int cross = (dx1*dy2)-(dx2*dy1);
		boolean ccw = (cross>0);
		if (ccw){
			x[0] = x1; x[1] = x2; x[2] = x3;
			y[0] = y1; y[1] = y2; y[2] = y3;
		} else{
			x[0] = x1; x[1] = x3; x[2] = x2;
			y[0] = y1; y[1] = y3; y[2] = y2;
		}			
	}

	/**
	 * Cette méthode teste si un point est à l'intérieur d'un triangle
	 * @param _x l'abscisse du point
	 * @param _y l'ordonnée du point
	 * @return vrai si le point est dans le triangle, faux sinon
	 */
	public boolean isInside(int _x, int _y){
		int vx2 = _x - x[0]; int vy2 = _y - y[0];
		int vx1 = x[1] - x[0]; int vy1 = y[1] - y[0];
		int vx0 = x[2] - x[0]; int vy0 = y[2] - y[0];

		int dot00 = vx0 * vx0 + vy0 * vy0;
		int dot01 = vx0 * vx1 + vy0 * vy1;
		int dot02 = vx0 * vx2 + vy0 * vy2;
		int dot11 = vx1 * vx1 + vy1 * vy1;
		int dot12 = vx1 * vx2 + vy1 * vy2;
		double invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
		return ((u > 0) && (v > 0) && (u + v < 1));
	}	
	
	/**
	 * Retourne les 3 vertices d'un triangle
	 * @return un tableau de Vec2 comportant les vertices du triangle
	 */
	public Vec2[] getVertices() {
		Vec2[] vertices = new Vec2[3];
		for (int i = 0; i < 3; i++) {
			vertices[i] = new Vec2(x[i], y[i]);
		}
		return vertices;
	}
	
	/**
	 * Retourne les 3 vertices d'un triangle à l'échelle du monde physique
	 * @return un tableau de Vec2 comportant les vertices du triangle
	 */
	public Vec2[] getScaledVertices() {
		Vec2[] vertices = new Vec2[3];
		for (int i = 0; i < 3; i++) {
			vertices[i] = new Vec2(x[i], y[i]).mul(PhysicsWorld.PTM_RATIO);
		}
		return vertices;
	}
	
}
