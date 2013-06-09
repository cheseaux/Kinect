package gestures;

import java.awt.Point;

import javax.swing.event.EventListenerList;

/**
 * Get hands position in skeleton projection: [0 ; 640] x [0 ; 480]
 * @author Jonathan Cheseaux et William Trouleau
 */
public class HandGesture {

	/** Liste des observer de cette gesture */
	private final EventListenerList listeners = new EventListenerList();
	
	/** Distance maximum pour détecter un click */
	private static final int MAX_TOLERANCE = 480;
	
	/** Distance minimale pour détecter le release */
	private static final int MIN_TOLERANCE = 300;
	
	/** Coefficient de vitesse */
	private static final int EPSILON = 25;

	/** Boolean indiquant l'état des évènements en cours */
	private boolean leftHandPressed = false;
	private boolean rightHandPressed = false;
	private boolean multiPressed = false;

	/** Position de la main gauche et droite */
	private Point mouseLeftPos = null;
	private Point mouseRightPos = null;

	/** Historique des profondeurs des main */
	private int oldLeftDepth = -1;
	private int oldRightDepth = -1;
	
	/** ID de l'utilisateur */
	private int userID;

	public HandGesture(int userID) {
		this.userID = userID;
	}

	public Point getMouseLeftPos() {
		return mouseLeftPos;
	}

	public Point getMouseRightPos() {
		return mouseRightPos;
	}
	
	public int getUserID() {
		return userID;
	}

	/**
	 * Update the new coordinates of the hands and generate (if needed) a new "mouse" event
	 * @param depthLeft, depth of the left hand
	 * @param posLeft, position of the left hand
	 * @param ref, reference depth
	 * @param depthRight, depth of the right hand
	 * @param posRight, position of the right hand
	 */
	public void updateDepth(int depthLeft, Point posLeft, int ref,
			int depthRight, Point posRight) {
		
		//Ensure that the movement is enough fast to be detected
		//NOTE : slow movement are not considered as MousePressed/Clicked event
		//This is to prevent misclicks
		boolean oldLeftClose = Math.abs(depthLeft - oldLeftDepth) <= EPSILON;
		boolean oldRightClose = Math.abs(depthRight - oldRightDepth) <= EPSILON;

		oldLeftDepth = depthLeft;
		oldRightDepth = depthRight;
		mouseLeftPos = posLeft;
		mouseRightPos = posRight;

		// Différence of depth between hands and head
		int diffLeft = ref - depthLeft;
		int diffRight = ref - depthRight;

		
		if (!oldLeftClose || leftHandPressed) {
			if (diffLeft > MAX_TOLERANCE) {
				if (!leftHandPressed) {
					fireMouseLeftClicked((int) posLeft.getX(), (int) posLeft.getY());
					leftHandPressed = true;
				} else {
					fireMouseLeftPressed((int) posLeft.getX(), (int) posLeft.getY());
				}
			} else if (diffLeft < MIN_TOLERANCE){
				if (leftHandPressed) {
					fireMouseLeftReleased(posLeft.getX(), posLeft.getY());
					leftHandPressed = false;
				}
			} 
		}
		if (!oldRightClose || rightHandPressed) {
			if (diffRight > MAX_TOLERANCE) {
				if (!rightHandPressed) {
					fireMouseRightClicked((int) posRight.getX(), (int) posRight.getY());
					rightHandPressed = true;
				} else {
					fireMouseRightPressed((int) posRight.getX(), (int) posRight.getY());
				}
			} else if (diffRight < MIN_TOLERANCE) {
				if (rightHandPressed) {
					fireMouseRightReleased(posRight.getX(), posRight.getY());
					rightHandPressed = false;
				}
			}
		}

		if ((!oldLeftClose && !oldRightClose) || multiPressed) {
			if ((diffLeft > MAX_TOLERANCE) && (diffRight > MAX_TOLERANCE)) {
				fireTwoHands(posLeft.getX(), posLeft.getY(), posRight.getX(), posRight.getY());
				multiPressed = true;
			} else if ((diffLeft < MIN_TOLERANCE) && (diffRight < MIN_TOLERANCE)){
				if (multiPressed) {
					fireTwoHandsReleased(posLeft.getX(), posLeft.getY(), posRight.getX(), posRight.getY());
					multiPressed = false;
				}
			}
		}
	}

	public boolean containsListener(KinectMouseListener listener) {
		for (KinectMouseListener listen : getKinectMouseListeners()) {
			if (listen == listener) {
				return true;
			}
		}
		return false;
	}

	public void addKinectMouseListener(KinectMouseListener listener) {
		if (containsListener(listener)) {
			return;
		}
		listeners.add(KinectMouseListener.class, listener);
	}

	public void removeKinectMouseListener(KinectMouseListener listener) {
		if (!containsListener(listener)) {
			return;
		}
		listeners.remove(KinectMouseListener.class, listener);
	}

	private KinectMouseListener[] getKinectMouseListeners() {
		return listeners.getListeners(KinectMouseListener.class);
	}

	private void fireMouseRightPressed(int x, int y) {
		if (multiPressed) {
			return;
		}
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.mouseRightPressed(userID, (int) x, (int) y);
		}
	}

	private void fireMouseLeftPressed(int x, int y) {
		if (multiPressed) {
			return;
		}
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			
			listener.mouseLeftPressed(userID, (int) x, (int) y);
		}
	}
	
	private void fireTwoHands(double d, double e, double f, double g){
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.multiPressed(userID, (int) d, (int) e, (int) f, (int) g);
		}
	}

	private void fireTwoHandsReleased(double d, double e, double f, double g){
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.multiReleased(userID, (int) d, (int) e, (int) f, (int) g);
		}
	}

	private void fireMouseLeftClicked(double x, double y) {
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.mouseLeftClicked(userID, (int) x, (int) y);
		}
	}

	private void fireMouseRightClicked(double x, double y) {
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.mouseRightClicked(userID, (int) x, (int) y);
		}
	}

	private void fireMouseLeftReleased(double d, double e) {
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.mouseLeftReleased(userID, (int) d, (int) e);
		}
	}

	private void fireMouseRightReleased(double x, double y) {
		for (KinectMouseListener listener : getKinectMouseListeners()) {
			listener.mouseRightReleased(userID, (int) x, (int) y);
		}
	}

}
