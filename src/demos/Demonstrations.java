package demos;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import gestures.HandGesture;
import gestures.KinectMouseListener;

import java.io.IOException;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import kinect.KinectModule;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;
import org.jbox2d.common.Vec2;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import text.TextDisplay;

/**
 * Cette classe abstraite rassemble les éléments/méthodes communs à toutes les démonstrations
 * et forcent les sous-classe à implémenter les méthode de dessin, etc...
 * Cette classe implémente le KinectMouseListener pour laisser le choix aux sous-classes d'implémenter
 * les méthodes liées aux évènements ou non (pour éviter d'avoir des méthodes dont le corps est vide
 * dans chaque sous-classe)
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public abstract class Demonstrations extends Observable implements Observer, KinectMouseListener{

	/** Visiblité de la démonstration */
	private boolean visible = false;
	
	/** Définit si les curseurs doivent être dessinés */
	private boolean drawCursor = false;
	
	/** Si définit à true, empêche le menu de s'afficher */
	private boolean blockMenu = false;

	/** Définit si le skelette doit être dessiné */
	private boolean drawSkeleton = false;
	
	/** Définit si l'avatar doit être dessiné */
	private boolean drawAvatar = false;

	/** Module commun aux démonstrations */
	protected DemonstrationsCommon common;
	
	/** Module de la Kinect */
	protected KinectModule kinectModule;
	
	/** Texture du curseur de la main */
	protected Texture handIcon;

	/** Type de démonstration (Physics1, Physics2, KinectDemonstration etc...) */
	private DemoType demoType;

	/**
	 * Construit une nouvelle instance de Démonstration
	 * @param type, le type de démonstration
	 * @see DemoType
	 */
	public Demonstrations(DemoType type) {
		demoType = type;
		common = DemonstrationsCommon.getInstance();
		kinectModule = KinectModule.getInstance();
		try {
			handIcon = common.getHandCursorTexture();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DemoType getDemoType() {
		return demoType;
	}

	public void setCursorVisible(boolean b) {
		drawCursor = b;
	}

	public void setSkeletonVisible(boolean b) {
		this.drawSkeleton = b;
	}
	
	public void setAvatarVisible(boolean b) {
		this.drawAvatar = b;
	}

	public boolean skeletonIsDrawn() {
		return drawSkeleton;
	}

	public boolean avatarIsDrawn() {
		return drawAvatar;
	}

	public boolean isVisible() {
		return visible;
	}

	/**
	 * Modifie la visiblité de la démonstration
	 * @param visible, si true, affiche la démonstration, la masque sinon
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible) {
			common.subscribeToHandGestures(this);
		} else {
			common.unSubscribeToHandGestures(this);
		}
	}

	/**
	 * Invoque les appels clavier
	 * @param key, la touche pressée sur le clavier de l'utilisateur
	 */
	public void pollInput(String key) {
		if (visible) {
			pollInput(key);
		}
	}

	/**
	 * Définit les projections matricielles pour OpenGL
	 */
	public abstract void setProjection();

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Menu && arg instanceof Boolean) {
			if (((Boolean) arg)) {
				common.subscribeToHandGestures(this);
			} else {
				common.unSubscribeToHandGestures(this);
			}
		}
	}

	/**
	 * Dessine la démonstration, le squelette, le curseur etc...
	 * @throws Exception, si un problème est décelé au niveau de la Kinect
	 */
	public void drawDemonstration() throws Exception {
		if (visible) {

			setProjection();
			draw();

			if (drawCursor || drawSkeleton) {
				
				kinectModule.updateDepth();
				setSkeletonProjection();
				
				for (int userID : kinectModule.getUsers()) {
					try {
						if (kinectModule.isSkeletonReady(userID)) {
							if (drawSkeleton) {
								drawSkeletonBox(userID);
							}
							if (drawAvatar) {
								drawAvatar(userID);
							}
							if (drawCursor) {
								Iterator<HandGesture> it = common.getGestureIterator();
								while (it.hasNext()) {
									HandGesture gesture = it.next();
									if (gesture.getMouseLeftPos() != null && gesture.getMouseRightPos() != null) {
										drawHands(userID);
									}
								}
							}
						}
						
					} catch (StatusException e) {
						System.err.println("Status exception while drawing skeleton of user " + userID);
					}
				}

			}

		}
	}

	/**
	 * Méthode de dessin pour tous les éléments non-communs aux démonstrations
	 * @throws Exception, si un problème avec la Kinect est decelé
	 */
	public abstract void draw() throws Exception;

	/**
	 * Récupére la position des mains d'un utilisateur et invoque la méthode les dessinant à l'écran
	 * @param userID, l'ID de l'utilisateur
	 * @throws Exception, si un problème avec la Kinect est decelé
	 */
	private void drawHands(int userID) throws Exception {
		SkeletonJointPosition left = kinectModule.getJointsMap(userID).get(SkeletonJoint.LEFT_HAND);
		Vec2 leftPos = new Vec2(left.getPosition().getX(), 480 - left.getPosition().getY());
		
		SkeletonJointPosition right = kinectModule.getJointsMap(userID).get(SkeletonJoint.RIGHT_HAND);
		Vec2 rightPos = new Vec2(right.getPosition().getX(), 480 - right.getPosition().getY());
		
		drawHand(leftPos.x, leftPos.y);
		drawHand(rightPos.x, rightPos.y);
	}

	/**
	 * Dessine les curseurs à l'écran à l'emplacement des mains de l'utilisateur
	 * @param x, abscisse de la main
	 * @param y, ordonnée de la main
	 * @throws Exception, si une problème avec la Kinect est decelé
	 */
	private void drawHand(float x, float y) throws Exception {
		if (!drawCursor) {
			return;
		}
		handIcon.bind();
		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glPushMatrix();
		GL11.glTranslatef(x,y, 1.0f);
		GL11.glScalef(25f,25f,1f); 

		GL11.glBegin(GL_QUADS);
		{
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex3f(-1, -1, 0);

			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(1, -1, 0);

			GL11.glTexCoord2f(0, 1);
			GL11.glVertex3f(1, 1, 0);

			GL11.glTexCoord2f(1, 1);
			GL11.glVertex3f(-1, 1, 0);
		}

		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
	}
	
	/**
	 * Draw a box between two given joints position
	 */
	private void drawBox(int userID, SkeletonJoint joint1, SkeletonJoint joint2, Texture tex, float scale) {
		SkeletonJointPosition pos = kinectModule.getJointsMap(userID).get(joint1);
		Vec2 pos1 = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());

		pos = kinectModule.getJointsMap(userID).get(joint2);
		Vec2 pos2 = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());

		// get normalize axes between the 2 joints
		Vec2 axe = pos1.sub(pos2);
		axe.normalize();

		Vec2 normal = new Vec2(-axe.y, axe.x);

		normal.mulLocal(scale);

		Vec2 p1 = pos1.add(normal);
		Vec2 p2 = pos1.sub(normal);
		Vec2 p4 = pos2.add(normal);
		Vec2 p3 = pos2.sub(normal);

		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		if (tex != null) {
			tex.bind();
		}
		
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(p1.x, p1.y, 0);
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(p2.x, p2.y, 0);
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(p3.x, p3.y, 0);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(p4.x, p4.y, 0);
		GL11.glEnd();
		
		GL11.glDisable(GL11.GL_BLEND);
	}


	/**
	 * Dessine une tête virtuelle à l'emplacement de la tête de l'utilisateur
	 * @param userID l'ID de l'utilisateur
	 * @param head le joint de la tête récupéré par la Kinect
	 * @param neck le joint du cou récupéré par la Kinect
	 * @param tex la texture à afficher pour la tête
	 */
	private void drawHead(int userID, SkeletonJoint head, SkeletonJoint neck, Texture tex) {
		SkeletonJointPosition neckPos = kinectModule.getJointsMap(userID).get(neck);
		SkeletonJointPosition headPos = kinectModule.getJointsMap(userID).get(head);
		
		Vec2 pos2 = new Vec2(neckPos.getPosition().getX(), 480 - neckPos.getPosition().getY());
		Vec2 pos1 = new Vec2(headPos.getPosition().getX(), 480 - headPos.getPosition().getY());
		
		Vec2 axis = pos1.sub(pos2);
		axis.normalize();
		Vec2 vert = new Vec2(0,1);
		double angle = Math.acos( Vec2.dot(axis, vert) );

		float depth = headPos.getPosition().getZ();
		float scale = 60000/depth;
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		if (tex != null) {
			tex.bind();
		}
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glPushMatrix();
		GL11.glTranslatef(pos1.x, pos1.y-scale*0.8f, 0);
		GL11.glRotated(angle, 0, 0, 1);
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(scale, scale, 0);
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(-scale, scale, 0);
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(-scale, -scale, 0);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(scale, -scale, 0);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_BLEND);
		
	}

	/**
	 * Dessine le squelette de l'utilisateur à l'écran
	 * @param user, l'utilisateur
	 * @throws StatusException, si un problème est decelé au niveau de la Kinect
	 */
	private void drawSkeletonBox(int user) throws StatusException
	{
		float scale = 7.0f;
		
		kinectModule.getJoints(new Integer(user));

		drawBox(user, SkeletonJoint.HEAD, SkeletonJoint.NECK, common.getAvatar(), scale);
		drawHead(user, SkeletonJoint.HEAD, SkeletonJoint.NECK, common.getAvatar_head());
		
		drawBox(user, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.RIGHT_SHOULDER, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_HIP, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_SHOULDER, common.getAvatar(), scale);

		drawBox(user, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.LEFT_HAND, SkeletonJoint.LEFT_ELBOW, common.getAvatar(), scale);

		drawBox(user, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.RIGHT_HAND, SkeletonJoint.RIGHT_ELBOW, common.getAvatar(), scale);

		drawBox(user, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT, common.getAvatar(), scale);

		drawBox(user, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE, common.getAvatar(), scale);
		drawBox(user, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT, common.getAvatar(), scale);
		
	}

	
	/**
	 * Dessine une personnage en 2D à la place de l'utilisateur
	 * @param user, l'ID de l'user
	 * @throws StatusException, si un problème est rencontré lors de la récupération du squelette
	 */
	private void drawAvatar(int user) throws StatusException
	{	
		common.getIronman().bind();
		
		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		kinectModule.getJoints(new Integer(user));
		
        SkeletonJointPosition pos = kinectModule.getJointsMap(user).get(SkeletonJoint.HEAD);
		Vec2 head_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		head_pos = changeOfVariable(head_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.NECK);
		
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_SHOULDER);
		Vec2 left_shoulder_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		left_shoulder_pos = changeOfVariable(left_shoulder_pos);		
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_ELBOW);
		Vec2 left_elbow_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		left_elbow_pos = changeOfVariable(left_elbow_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_HAND);
		Vec2 left_hand_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//    	left_hand_pos = changeOfVariable(left_hand_pos);
		
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_SHOULDER);
		Vec2 right_shoulder_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_shoulder_pos = changeOfVariable(right_shoulder_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_ELBOW);
		Vec2 right_elbow_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_elbow_pos = changeOfVariable(right_elbow_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_HAND);
		Vec2 right_hand_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_hand_pos = changeOfVariable(right_hand_pos);
		
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_HIP);
		Vec2 left_hip_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		left_hip_pos = changeOfVariable(left_hip_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_KNEE);
		Vec2 left_knee_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		left_knee_pos = changeOfVariable(left_knee_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.LEFT_FOOT);
		Vec2 left_foot_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//    	left_foot_pos = changeOfVariable(left_foot_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_HIP);
		Vec2 right_hip_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_hip_pos = changeOfVariable(right_hip_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_KNEE);
		Vec2 right_knee_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_knee_pos = changeOfVariable(right_knee_pos);
		pos = kinectModule.getJointsMap(user).get(SkeletonJoint.RIGHT_FOOT);
		Vec2 right_foot_pos = new Vec2(pos.getPosition().getX(), 480 - pos.getPosition().getY());
//		right_foot_pos = changeOfVariable(right_foot_pos);
		
		double dt = 0.05;
		float dp = 25;
		
		// HEAD
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.3, 0.0); 		GL11.glVertex3f(left_shoulder_pos.x, 2*head_pos.y-left_shoulder_pos.y, 0);
		GL11.glTexCoord2d(0.3-dt, 0.2-dt);	GL11.glVertex3f(left_shoulder_pos.x-dp, left_shoulder_pos.y+dp, 0);
		GL11.glTexCoord2d(0.7+dt, 0.2-dt);	GL11.glVertex3f(right_shoulder_pos.x+dp, right_shoulder_pos.y+dp, 0);
		GL11.glTexCoord2d(0.7, 0.0); 		GL11.glVertex3f(right_shoulder_pos.x, 2*head_pos.y-right_shoulder_pos.y, 0);
        GL11.glEnd();
		
//		LEFT_ELBOW, 0.2, 0.37
//    	LEFT_HAND, 0.05, 0.525
		// LEFT ARM
		GL11.glBegin(GL11.GL_QUADS); // high part
		GL11.glTexCoord2d(0.2, 0.37-dt); 	GL11.glVertex3f(left_elbow_pos.x, left_elbow_pos.y+dp, 0);
		GL11.glTexCoord2d(0.2, 0.37+dt);	GL11.glVertex3f(left_elbow_pos.x, left_elbow_pos.y-dp, 0);
		GL11.glTexCoord2d(0.3-dt, 0.2+dt);	GL11.glVertex3f(left_shoulder_pos.x-dp, left_shoulder_pos.y-dp, 0);
		GL11.glTexCoord2d(0.3-dt, 0.2-dt);	GL11.glVertex3f(left_shoulder_pos.x-dp, left_shoulder_pos.y+dp, 0);
        GL11.glEnd();
		GL11.glBegin(GL11.GL_QUADS); // low part
		GL11.glTexCoord2d(0.05-dt, 0.525-dt); 	GL11.glVertex3f(left_hand_pos.x, left_hand_pos.y+dp, 0);
		GL11.glTexCoord2d(0.05-dt, 0.525+dt);	GL11.glVertex3f(left_hand_pos.x, left_hand_pos.y-dp, 0);
		GL11.glTexCoord2d(0.2, 0.37+dt);		GL11.glVertex3f(left_elbow_pos.x, left_elbow_pos.y-dp, 0);
		GL11.glTexCoord2d(0.2, 0.37-dt); 		GL11.glVertex3f(left_elbow_pos.x, left_elbow_pos.y+dp, 0);
        GL11.glEnd();
        
//      RIGHT_ELBOW, 0.8, 0.375
//    	RIGHT_HAND, 0.92, 0.51
		// RIGHT ARM
        GL11.glBegin(GL11.GL_QUADS); // high part
		GL11.glTexCoord2d(0.7+dt, 0.2-dt);	GL11.glVertex3f(right_shoulder_pos.x+dp, right_shoulder_pos.y+dp, 0);
        GL11.glTexCoord2d(0.7+dt, 0.2+dt);	GL11.glVertex3f(right_shoulder_pos.x+dp, right_shoulder_pos.y-dp, 0);
        GL11.glTexCoord2d(0.8, 0.375+dt);	GL11.glVertex3f(right_elbow_pos.x, right_elbow_pos.y-dp, 0);
        GL11.glTexCoord2d(0.8, 0.375-dt); 	GL11.glVertex3f(right_elbow_pos.x, right_elbow_pos.y+dp, 0);
        GL11.glEnd();
		GL11.glBegin(GL11.GL_QUADS); // low part
		GL11.glTexCoord2d(0.8, 0.375-dt); 		GL11.glVertex3f(right_elbow_pos.x, right_elbow_pos.y+dp, 0);
		GL11.glTexCoord2d(0.8, 0.375+dt);		GL11.glVertex3f(right_elbow_pos.x, right_elbow_pos.y-dp, 0);
		GL11.glTexCoord2d(0.92+dt, 0.51+dt);	GL11.glVertex3f(right_hand_pos.x, right_hand_pos.y-dp, 0);
		GL11.glTexCoord2d(0.92+dt, 0.51-dt); 	GL11.glVertex3f(right_hand_pos.x, right_hand_pos.y+dp, 0);
        GL11.glEnd();
        
//      LEFT_FOOT, 0.42, 0.95
//		LEFT_KNEE, 0.41, 0.71
        // LEFT LEG
        GL11.glBegin(GL11.GL_QUADS); // high part
        GL11.glTexCoord2d(0.4-dt, 0.5);		GL11.glVertex3f(left_hip_pos.x-dp, left_hip_pos.y, 0);
        GL11.glTexCoord2d(0.41-dt, 0.71);	GL11.glVertex3f(left_knee_pos.x-dp, left_knee_pos.y, 0);
        GL11.glTexCoord2d(0.41+dt, 0.71);	GL11.glVertex3f(left_knee_pos.x+dp, left_knee_pos.y, 0);
        GL11.glTexCoord2d(0.4+dt, 0.5);		GL11.glVertex3f(left_hip_pos.x+dp, left_hip_pos.y, 0);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS); // low part
        GL11.glTexCoord2d(0.41-dt, 0.71);		GL11.glVertex3f(left_knee_pos.x-dp, left_knee_pos.y, 0);
        GL11.glTexCoord2d(0.42-dt, 0.95+dt);	GL11.glVertex3f(left_foot_pos.x-dp, left_foot_pos.y-dp, 0);
        GL11.glTexCoord2d(0.42+dt, 0.95+dt);	GL11.glVertex3f(left_foot_pos.x+dp, left_foot_pos.y-dp, 0);
        GL11.glTexCoord2d(0.41+dt, 0.71);		GL11.glVertex3f(left_knee_pos.x+dp, left_knee_pos.y, 0);
        GL11.glEnd();
       
//		RIGHT_HIP, 0.6, 0.5
//      RIGHT_FOOT, 0.58, 0.95
//		RIGHT_KNEE, 0.59, 0.71
        // RIGHT LEG
        GL11.glBegin(GL11.GL_QUADS); // high part
        GL11.glTexCoord2d(0.6-dt, 0.5);		GL11.glVertex3f(right_hip_pos.x-dp, right_hip_pos.y, 0);
        GL11.glTexCoord2d(0.59-dt, 0.71);	GL11.glVertex3f(right_knee_pos.x-dp, right_knee_pos.y, 0);
        GL11.glTexCoord2d(0.59+dt, 0.71);	GL11.glVertex3f(right_knee_pos.x+dp, right_knee_pos.y, 0);
        GL11.glTexCoord2d(0.6+dt, 0.5);		GL11.glVertex3f(right_hip_pos.x+dp, right_hip_pos.y, 0);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS); // low part
        GL11.glTexCoord2d(0.59-dt, 0.71);		GL11.glVertex3f(right_knee_pos.x-dp, right_knee_pos.y, 0);
        GL11.glTexCoord2d(0.58-dt, 0.95+dt);	GL11.glVertex3f(right_foot_pos.x-dp, right_foot_pos.y-dp, 0);
        GL11.glTexCoord2d(0.58+dt, 0.95+dt);	GL11.glVertex3f(right_foot_pos.x+dp, right_foot_pos.y-dp, 0);
        GL11.glTexCoord2d(0.59+dt, 0.71);		GL11.glVertex3f(right_knee_pos.x+dp, right_knee_pos.y, 0);
        GL11.glEnd();
        
        // TORSO
        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glTexCoord2d(0.3-dt, 0.2-dt);	GL11.glVertex3f(left_shoulder_pos.x-dp, left_shoulder_pos.y+dp, 0);
        GL11.glTexCoord2d(0.3-dt, 0.2+dt);	GL11.glVertex3f(left_shoulder_pos.x-dp, left_shoulder_pos.y-dp, 0);
		GL11.glTexCoord2d(0.4-dt, 0.5);		GL11.glVertex3f(left_hip_pos.x-dp, left_hip_pos.y, 0);
		GL11.glTexCoord2d(0.6+dt, 0.5);		GL11.glVertex3f(right_hip_pos.x+dp, right_hip_pos.y, 0);
		GL11.glTexCoord2d(0.7+dt, 0.2+dt);	GL11.glVertex3f(right_shoulder_pos.x+dp, right_shoulder_pos.y-dp, 0);
		GL11.glTexCoord2d(0.7+dt, 0.2-dt);	GL11.glVertex3f(right_shoulder_pos.x+dp, right_shoulder_pos.y+dp, 0);
		GL11.glEnd();
	}
	

	/**
	 * Skeleton Projection
	 * 
	 * Orthogonal projection: [0 ; 640] x [0 ; 480]
	 * 
	 * WARNING: coordinates of Kinect images, need the following change of variable to 
	 * interact with usual orthogonal projection :
	 * x = x*Display.getWidth()/640;
	 * y = Display.getHeight() - y*Display.getHeight()/480;
	 * 
	 */
	private void setSkeletonProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 640, 0, 480, 1, -1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	/**
	 * Autorise le menu à s'afficher
	 */
	protected void enableMenu() {
		if (blockMenu) {
			return;
		}
		blockMenu = true;
		setChanged();
		notifyObservers(new Boolean(blockMenu));
		TextDisplay.println("Menu should be enabled now");
	}
	
	/**
	 * Désactive temporairement l'affichage du menu
	 */
	protected void disableMenu() {
		if (!blockMenu) {
			return;
		}
		blockMenu = false;
		setChanged();
		notifyObservers(new Boolean(blockMenu));
		TextDisplay.println("Menu should be disabled now");
	}

	@Override
	public void mouseLeftReleased(int userID, int posx, int posy) {
		enableMenu();
	}

	@Override
	public void mouseLeftPressed(int userID, int posx, int posy) {
		disableMenu();
	}

	@Override
	public void mouseRightReleased(int userID, int posx, int posy) {
		enableMenu();
	}

	@Override
	public void mouseRightPressed(int userID, int posx, int posy) {
		disableMenu();
	}

	@Override
	public void multiPressed(int userID, int leftx, int lefty, int rightx, int righty) {
		disableMenu();
	}

	@Override
	public void multiReleased(int userID, int leftx, int lefty, int rightx, int righty) {
		enableMenu();
	}

	@Override
	public void mouseLeftClicked(int userID, int x, int y) {
		enableMenu();
	}

	@Override
	public void mouseRightClicked(int userID, int x, int y) {
		enableMenu();
	}
}
