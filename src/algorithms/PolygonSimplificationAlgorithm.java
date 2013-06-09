package algorithms;

import java.awt.Polygon;
import java.awt.geom.Line2D;


/**
 * Cette classe implémente l'algorithm de simplification de polygones basé selon
 * l'algorithm de Ramer-Douglas Peucker.
 * Voir (http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm)
 * Le but principal étant de réduire le nombre de vertex pour des raisons
 * de performances avec OpenGL
 * @author Jonathan Cheseaux & William Trouleau
 *
 */
public class PolygonSimplificationAlgorithm {

	/** Tolerance de l'algorithm */
	private double tolerance;

	/**
	 * Construit une nouvelle instance de l'algorithme
	 * @param tolerance, la tolerance de l'algorithme
	 */
	public PolygonSimplificationAlgorithm(double tolerance) {
		if (tolerance <= 0) {
			throw new IllegalArgumentException("Tolerance must be greater than zero");
		}
		this.tolerance = tolerance;
	}
	
	/**
	 * Cette méthode réduit le nombre de vertex d'un poylgone
	 * @param original, le polygon à simplifier
	 * @return un polygon simplifié comportant moins de vertices que l'original
	 */
	public Polygon simplifyPolygon(Polygon original) {
		Polygon2D result = simplifyPolygon(new Polygon2D(original.xpoints, original.ypoints));
		if (result.npoints < 2) {
			return original;
		}
		Polygon polygon = new Polygon();
		polygon.xpoints = result.xpoints.clone();
		polygon.ypoints = result.ypoints.clone();
		polygon.npoints = result.npoints;
		return polygon;
	}

	/**
	 * Cette méthode réduit le nombre de vertex d'un poylgone
	 * @param original, le polygon à simplifier
	 * @return un polygon simplifié comportant moins de vertices que l'original
	 */
	public Polygon2D simplifyPolygon(Polygon2D original) {
		double dmax = 0;
		int index = 0;
		int xPos0 = original.getXPoint(0);
		int yPos0 = original.getYPoint(0);
		int xPosEnd = original.getXPoint(original.npoints - 1);
		int yPosEnd = original.getYPoint(original.npoints - 1);
		
		for (int i = 2; i < original.npoints; i++) {
			double d = Line2D.ptLineDist(xPos0, yPos0, xPosEnd, yPosEnd, original.getXPoint(i), original.getYPoint(i));
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		Polygon2D result;
		if (dmax >= tolerance) {
			Polygon2D res1 = simplifyPolygon(original.polygonPart(0, index));
			Polygon2D res2 = simplifyPolygon(original.polygonPart(index, original.npoints));
			result = Polygon2D.merge(res1.polygonPart(0, res1.npoints-1),res2.polygonPart(0, res2.npoints));
		} else {
			result = new Polygon2D();
			result.addPoint(xPos0, yPos0);
			result.addPoint(xPosEnd, yPosEnd);
		}

		// Renvoie le nouveau résultat
		return result.eraseZeros();
	}
}

