package algorithms;

import java.awt.Point;
import java.awt.Polygon;

public class Polygon2D extends Polygon {

	private static final long serialVersionUID = -8063292158997334099L;
	
	public Polygon2D(int[] xpoints, int[] ypoints) {
		if (xpoints.length != ypoints.length) {
			throw new IllegalArgumentException("Array dimensions should be the same !");
		}
		this.xpoints = xpoints.clone();
		this.ypoints = ypoints.clone();
		this.npoints = xpoints.length;
	}
	
	public Polygon2D() {
		super();
	}
	
	public static Polygon2D merge(Polygon2D poly1, Polygon2D poly2) {
		Polygon2D poly = new Polygon2D(poly1.xpoints, poly1.ypoints);
		for (int i = 0; i < poly2.npoints; i++) {
			poly.addPoint(poly2.xpoints[i], poly2.ypoints[i]);
		}
		return poly;
	}
	
	public Polygon2D eraseZeros() {
		Polygon2D poly = new Polygon2D();
		for (int i = 0; i < npoints; i++) {
			if (xpoints[i] != 0.0 && ypoints[i] != 0.0)  {
				poly.addPoint(xpoints[i], ypoints[i]);
			}
		}
		return poly;
	}
	
	public Polygon2D polygonPart(int start, int length) {
		Polygon2D result = new Polygon2D();
		for (int i = start; i < length && i < npoints; i++) {
			result.addPoint(xpoints[i], ypoints[i]);
		}
		return result;
	}

	public Point getPoint(int index) {
		if (index >= this.npoints) {
			throw new IndexOutOfBoundsException("Index : " + index + " > " + " Array size : " + this.npoints);
		}
		Point result = new Point();
		result.setLocation(xpoints[index], ypoints[index]);
		return result;
	}
	
	public int getXPoint(int index) {
		if (index >= this.npoints) {
			throw new IndexOutOfBoundsException("Index : " + index + " > " + " Array size : " + this.npoints);
		}
		return this.xpoints[index];
	}
	
	public int getYPoint(int index) {
		if (index >= this.npoints) {
			throw new IndexOutOfBoundsException("Index : " + index + " > " + " Array size : " + this.npoints);
		}
		return this.ypoints[index];
	}
	
	public void setXPoint(int index, int val) {
		if (index >= this.npoints) {
			throw new IndexOutOfBoundsException("Index : " + index + " > " + " Array size : " + this.npoints);
		}
		this.xpoints[index] = val;
	}
	
	public void setYPoint(int index, int val) {
		if (index >= this.npoints) {
			throw new IndexOutOfBoundsException("Index : " + index + " > " + " Array size : " + this.npoints);
		}
		this.ypoints[index] = val;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < npoints; i++) {
			sb.append("(" + xpoints[i] + "," + ypoints[i] + "); ");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	
	
}

