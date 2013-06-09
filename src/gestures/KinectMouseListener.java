package gestures;

import java.util.EventListener;

/**
 * Cette interface doit être implémentée par toutes les démonstrations qui désirent
 * recevoir les notifications des gestures
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public interface KinectMouseListener extends EventListener{
	
	void mouseLeftReleased(int userID, int posx, int posy);
	void mouseLeftPressed(int userID, int posx, int posy);
	void mouseRightReleased(int userID, int posx, int posy);
	void mouseRightPressed(int userID, int posx, int posy);
	void multiPressed(int userID, int leftx, int lefty, int rightx, int righty);
	void multiReleased(int userID, int leftx, int lefty, int rightx, int righty);
	void mouseLeftClicked(int userID, int x, int y);
	void mouseRightClicked(int userID, int x, int y);
}
