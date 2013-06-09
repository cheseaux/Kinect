package algorithms;

import java.awt.Polygon;
import java.util.ArrayList;

/** 
 * Based on JSFL util by mayobutter (Box2D Forums)
 * and  Eric Jordan (http://www.ewjordan.com/earClip/) Ear Clipping experiment in Processing
 * 
 * Tarwin Stroh-Spijer - Touch My Pixel - http://www.touchmypixel.com/
 * 
 * Traduction d'un code ActionScript3 en java
 * Algorithme de triangulation de polygone basé sur l'Ear Clipping
 * */
public class Triangulator {
	
	/**
	 * Triangule le polygone passé en paramètre
	 * @param poly, le poylgon à trianguler
	 * @return un tableau de triangles
	 */
	public static Triangle[] triangulatePolygon(Polygon poly) {
		int[] xA = new int[poly.npoints];
		int[] yA = new int[poly.npoints];

		for (int i = 0; i < poly.npoints; i++) {
			xA[i] = poly.xpoints[i];
			yA[i] = poly.ypoints[i];
		}
		return(triangulatePolygonFromFlatArray(xA, yA));
	}

	private static Triangle[] triangulatePolygonFromFlatArray(int[] xv, int[] yv) {
		if (xv.length < 3 || yv.length < 3 || yv.length != xv.length) {
			return null;
		}

		int i = 0;
		int vNum = xv.length;

		ArrayList<Triangle> buffer = new ArrayList<Triangle>();
		int bufferSize = 0;

		int[] xrem = new int[vNum];
		int[] yrem = new int[vNum];

		for (i = 0; i < vNum; ++i) {
			xrem[i] = xv[i];
			yrem[i] = yv[i];
		}

		while (vNum > 3){
			//Find an ear
			int earIndex = -1;
			for (i = 0; i < vNum; ++i) {
				if (isEar(i, xrem, yrem)) {
					earIndex = i;
					break;
				}
			}

			//If we still haven't found an ear, we're screwed.
			//The user did Something Bad, so return null.
			//This will probably crash their program, since
			//they won't bother to check the return value.
			//At this we shall laugh, heartily and with great gusto.
			if (earIndex == -1) {
				return null;
			}

			//Clip off the ear:
			//  - remove the ear tip from the list

			//Opt note: actually creates a new list, maybe
			//this should be done in-place instead.  A linked
			//list would be even better to avoid array-fu.
			--vNum;
			int[] newx = new int[vNum];
			int[] newy = new int[vNum];
			int currDest = 0;

			for (i = 0; i < vNum; ++i) {
				if (currDest == earIndex) ++currDest;
				newx[i] = xrem[currDest];
				newy[i] = yrem[currDest];
				++currDest;
			}


			//  - add the clipped triangle to the triangle list
			int under = (earIndex == 0)?(xrem.length - 1):(earIndex - 1);
			int over = (earIndex == xrem.length - 1)?0:(earIndex + 1);
			Triangle toAdd = new Triangle(xrem[earIndex], yrem[earIndex], xrem[over], yrem[over], xrem[under], yrem[under]);
			buffer.add(toAdd);
			++bufferSize;

			//  - replace the old list with the new one
			xrem = newx;
			yrem = newy;
		}

		Triangle toAddMore = new Triangle(xrem[1], yrem[1], xrem[2], yrem[2], xrem[0], yrem[0]);
		buffer.add(toAddMore);
		++bufferSize;

		Triangle[] result = new Triangle[bufferSize];
		for (i = 0; i < bufferSize; i++) {
			result[i] = buffer.get(i);
		}

		return result;
	}
	
	/**
	 * This method invert the order of the polygon's vertices
	 * If the polygon was CCW then it becomes CW and vice versa
	 * @param poly, the polygon to inverse
	 * @return the inversed-order polygon
	 */
	public static Polygon invertClockOrder(Polygon poly) {
		int[] newx = new int[poly.npoints];
		int[] newy = new int[poly.npoints];
		int count = 0;
		for (int i = poly.npoints -1; i >= 0; i--) {
			newx[count] = poly.xpoints[i];
			newy[count] = poly.ypoints[i];
			count++;
		}
		return new Polygon(newx, newy, poly.npoints);
	}

	//Checks if vertex i is the tip of an ear
	/*
	 * */
	public static boolean isEar(int i , int[] xv, int[] yv)
	{
		int dx0 = 0;
		int dy0 = 0;
		int dx1 = 0;
		int dy1 = 0;
		dx0 = dy0 = dx1 = dy1 = 0;
		if (i >= xv.length || i < 0 || xv.length < 3) {
			return false;
		}
		int upper = i + 1;
		int lower = i - 1;
		if (i == 0){
			dx0 = xv[0] - xv[xv.length - 1];
			dy0 = yv[0] - yv[yv.length - 1];
			dx1 = xv[1] - xv[0];
			dy1 = yv[1] - yv[0];
			lower = xv.length - 1;
		} else if (i == xv.length - 1) {
			dx0 = xv[i] - xv[i - 1];
			dy0 = yv[i] - yv[i - 1];
			dx1 = xv[0] - xv[i];
			dy1 = yv[0] - yv[i];
			upper = 0;
		} else{
			dx0 = xv[i] - xv[i - 1];
			dy0 = yv[i] - yv[i - 1];
			dx1 = xv[i + 1] - xv[i];
			dy1 = yv[i + 1] - yv[i];
		}

		int cross = (dx0*dy1)-(dx1*dy0);
		if (cross > 0) {
			return false;
		}
		Triangle myTri = new Triangle(xv[i], yv[i], xv[upper], yv[upper], xv[lower], yv[lower]);

		for (int j = 0; j < xv.length; ++j) {
			if (!(j == i || j == lower || j == upper)) {
				if (myTri.isInside(xv[j], yv[j])) {
					return false;
				}
			}
		}
		return true;
	}



}
