package demos;

import gestures.HandGesture;
import gestures.KinectMouseListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import text.TextDisplay;

/**
 * Cette classe rassemble les éléments communs à toutes les démonstrations
 * comme la gestion des gestures de chaque utilisateur, des textures, du curseur des mains etc..
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class DemonstrationsCommon {
	
	/** Singleton instance */
	private static DemonstrationsCommon instance = null;
	
	/** Chemin d'accès au fichier image des main */
	private static final String HAND_CURSOR_PATH = "data/hand.png";
	
	/** Texture du curseur de la main */
	private Texture handCursorTexture = null;
	
	/** HashMap liant un utilisateur à ses gestures */
	private HashMap<Integer, HandGesture> userGesture = new HashMap<Integer, HandGesture>();
	
	/** Définit toutes les démonstrations qui se sont abonnés au KinectMouseListener */
	private ArrayList<KinectMouseListener> subscribers = new ArrayList<KinectMouseListener>();
	
	/** Textures 2D */
	private Texture avatar;
	private Texture avatar_head;
	private Texture ironman;
	
	private DemonstrationsCommon() {
		try {
			avatar = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream("data/avatar/metalStick_gold.png"));
			avatar_head = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream("data/avatar/head_gold.png"));
			ironman = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream("data/ironman.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ajoute un HandGesture pour un nouvel utilisateur
	 * @param userID, l'ID de l'utilisateur
	 */
	public synchronized void addUserGesture(Integer userID) {
		if (!userGesture.containsKey(userID)) {
			userGesture.put(userID, new HandGesture(userID));
			for (KinectMouseListener listener : subscribers) {
				userGesture.get(userID).addKinectMouseListener(listener);
			}
		}
	}
	
	/**
	 * Supprime les HandGesture d'un utilisateur (si celui-ci quitte le champ
	 * de vision de la Kinect)
	 * @param userID, l'id de l'utilisateur
	 */
	public synchronized void removeUserGesture(Integer userID) {
		HandGesture gesture = userGesture.get(userID);
		if (gesture != null) {
			for (KinectMouseListener listener : subscribers) {
				//Not very useful, since the gesture will be deleted
				//but better be cautious...
				gesture.removeKinectMouseListener(listener);
			}
			userGesture.remove(userID);
		}
	}
	
	
	public synchronized Iterator<HandGesture> getGestureIterator() {
		return userGesture.values().iterator();
	}

	/**
	 * Abonne une démonstration aux HandGesture de tous les utilisateurs
	 * @param listener, la démonstration
	 */
	public synchronized void subscribeToHandGestures(KinectMouseListener listener) {
		if (!subscribers.contains(listener)) {
			subscribers.add(listener);
			if (listener instanceof PhysicsDemonstration) {
				TextDisplay.println("Physics ACTIVATED");
			}
			for (HandGesture gesture : userGesture.values()) {
				gesture.addKinectMouseListener(listener);
			}
		}
	}
	
	/**
	 * Désabonne une démonstration aux HandGesture de tous les utilisateurs
	 * @param listener, la démonstration
	 */
	public synchronized void unSubscribeToHandGestures(KinectMouseListener listener) {
		if (subscribers.contains(listener)) {
			subscribers.remove(listener);
			if (listener instanceof PhysicsDemonstration) {
				TextDisplay.println("Physics DEACTIVATED");
			}
			for (HandGesture gesture : userGesture.values()) {
				gesture.removeKinectMouseListener(listener);
			}
		}
	}
	
	public synchronized HandGesture getUserGesture(Integer userID) {
		return userGesture.get(userID);
	}
	
	
	public synchronized static DemonstrationsCommon getInstance() {
		if (instance == null) {
			instance = new DemonstrationsCommon();
		}
		return instance;
	}
	
	public Texture getHandCursorTexture() throws IOException {
		if (handCursorTexture == null) {
			handCursorTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(HAND_CURSOR_PATH));
		}
		return handCursorTexture;
	}

	public Texture getAvatar() {
		return avatar;
	}

	public Texture getAvatar_head() {
		return avatar_head;
	}

	public Texture getIronman() {
		return ironman;
	}
	
}
