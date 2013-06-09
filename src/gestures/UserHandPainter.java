package gestures;

import java.awt.Polygon;
import java.util.ArrayList;

import org.jbox2d.common.Vec2;

/**
 * Cette classe permet de stocker les coordonnées de dessin par l'utilisateur
 * , de simplifier le poylgon dessinée et le trianguler pour l'ajouter au monde physique
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class UserHandPainter {

	/** L'ID de l'utilisateur */
	private int userID;

	/** Historiques des postitions des mains */
	private ArrayList<Vec2> histLeftHand = new ArrayList<Vec2>();
	private ArrayList<Vec2> histRightHand = new ArrayList<Vec2>();

	/** Définit si la capture des positions des mains est active ou non */
	boolean fetchLeft = false;
	boolean fetchRight = false;

	public UserHandPainter(int userID) {
		this.userID = userID;
	}

	public boolean isFetchLeft() {
		return fetchLeft;
	}

	public boolean isFetchRight() {
		return fetchRight;
	}

	public void updateLeft(int x, int y) {
		if (fetchLeft) {
			histLeftHand.add(new Vec2(x, y));
		}
	}

	public void updateRight(int x, int y) {
		if (fetchRight) {
			histRightHand.add(new Vec2(x, y));
		}
	}

	public void resetLeft() {
		histLeftHand.clear();
	}
	
	public void resetRight() {
		histRightHand.clear();
	}

	public int getUserID() {
		return userID;
	}

	public ArrayList<Vec2> getHistLeftHand() {
		return histLeftHand;
	}

	public ArrayList<Vec2> getHistRightHand() {
		return histRightHand;
	}
	
	/**
	 * Retourne le polygon dessiné par la main droite
	 * @return
	 */
	public Polygon getRightPolygon() {
		Polygon poly = new Polygon();
		for (Vec2 vertex : histRightHand) {
			poly.addPoint((int) vertex.x, (int) vertex.y);
		}
		return poly;
	}
	
	/**
	 * Retourne le poylgon dessiné par la main gauche
	 * @return
	 */
	public Polygon getLeftPolygon() {
		Polygon poly = new Polygon();
		for (Vec2 vertex : histLeftHand) {
			poly.addPoint((int) vertex.x, (int) vertex.y);
		}
		return poly;
	}

	public void enableLeft(boolean enabled) {
		fetchLeft = enabled;
	}

	public void enableRight(boolean enabled) {
		fetchRight = enabled;
	}

}
