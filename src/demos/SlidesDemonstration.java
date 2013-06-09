package demos;

import gestures.KinectMouseListener;
import gl.GLParameters;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;

/**
 * Cette classe représente la présentation avec slide du projet
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class SlidesDemonstration extends Demonstrations implements KinectMouseListener{

	/** La liste des slides à afficher */
	private ArrayList<Slide> slidesList;
	
	/** Le nombre de slides total*/
	private static int length;
	
	/** Le chemin d'accès au répertoire contenant les slides */
	private String location;

	/** Slide movement parameters */
	private boolean previousSlide;
	private boolean nextSlide;
	private double state;
	private final static int TOLERANCE = 50;
	private int currentSlide;
	private double speed;
	private double initialScale;
	private double currentSlideScale;
	private double currentSlideRotation;
	
	//Positions variables
	private int left_xpos;		// x coordinate of left hand when clicked
	private int right_xpos;		// x coordinate of right hand when clicked
	private int last_left_xpos;
	private int last_left_ypos;
	private int last_right_xpos;
	private int last_right_ypos;
	
	//Multipressed on ?
	private boolean isMultiOn = false;

	/**
	 * Construit la présentation avec le chemin d'accès au répertoire des slides et leur nombre
	 * @param slideShowLocation
	 * @param presentationLength
	 */
	public SlidesDemonstration(String slideShowLocation, int presentationLength){
		// demonstration parameters
		super(DemoType.SLIDES);
		setCursorVisible(true);
		setSkeletonVisible(false);
		setAvatarVisible(false);
		
		// transition parameters
		previousSlide = false;
		nextSlide = false;
		state = 0;

		// presentation parameters
		location = slideShowLocation;
		length = presentationLength;

		// gestures parameters
		last_left_xpos = -1;
		last_left_ypos = -1;
		last_right_xpos = -1;
		last_right_ypos = -1;
		
		initialScale = 6.0;
		currentSlideScale = initialScale; // initialize to 6.0 (same value as the other slides in draw method
		currentSlideRotation = 0.0;
		currentSlide = 0;
		speed = 0;

		// init slides
		slidesList = new ArrayList<Slide>(length);
		for (int i = 1; i <= length; i++) {
			Slide slide = new Slide(location + i + ".png", true);
			slidesList.add(slide);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		setSkeletonVisible(false);
		setAvatarVisible(false);
	}


	public int getCurrentSlide() {
		return currentSlide;
	}


	public void nextSlide() {
		if (currentSlide < length-1 && !previousSlide) {
			nextSlide = true;
		}
	}

	public void previousSlide() {
		if (currentSlide > 0 && !nextSlide) {
			previousSlide = true;
		}
	}

	/**
	 * Définis la projection OpenGL à utiliser (perspective)
	 */
	public void setProjection() {
		GL11.glViewport(0,0,Display.getWidth(), Display.getHeight());
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		float ratio = (float)Display.getWidth()/Display.getHeight();
		GLU.gluPerspective(45.0f, ratio,0.1f,100.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	/**
	 * Dessine les slides
	 */
	public void draw() throws Exception{
		double scale = initialScale;	// slide scaling
		double step = 17.0; 			// space between slides
		double angle = 0.0;
		double type = 0.75;				// slide type 3/4
		double ztrans = -30;

		double duration = 1;			// transition duration
		double delta = GLParameters.fps * duration;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearDepth(1.0f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);	
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,GL11.GL_NICEST);

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, 20); // move center of rotation

		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		if(currentSlide == 0){

			GL11.glPushMatrix();
			GL11.glRotated(-step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(-2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+2).drawPartialCylinder(3);
			GL11.glPopMatrix();

		} else if(currentSlide == 1){

			GL11.glPushMatrix();
			GL11.glRotated(-step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(-2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+2).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-1).drawPartialCylinder(3);
			GL11.glPopMatrix();

		} else if(currentSlide == length-1) {

			GL11.glPushMatrix();
			GL11.glRotated(step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-2).drawPartialCylinder(3);
			GL11.glPopMatrix();

		} else if(currentSlide == length-2) {

			GL11.glPushMatrix();
			GL11.glRotated(step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-2).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(-step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+1).drawPartialCylinder(3);
			GL11.glPopMatrix();

		} else {

			GL11.glPushMatrix();
			GL11.glRotated(step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(angle, 0, 1, 0);
			slidesList.get(currentSlide-2).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(-step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+1).drawPartialCylinder(3);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(-2.0*step+speed, 0, 1, 0);
			GL11.glTranslated(0, 0, ztrans);
			GL11.glScaled(scale, scale*type, 1);
			GL11.glRotated(-angle, 0, 1, 0);
			slidesList.get(currentSlide+2).drawPartialCylinder(3);
			GL11.glPopMatrix();
		}
		
		GL11.glPushMatrix();
		GL11.glRotated(speed, 0, 1, 0);
		GL11.glTranslated(0, 0, ztrans+currentSlideScale-initialScale);
		GL11.glRotated(currentSlideRotation, 0, 0, 1);
		GL11.glScaled(currentSlideScale, currentSlideScale*type, 1);
		slidesList.get(currentSlide).drawPartialCylinder(3);
		GL11.glPopMatrix();

		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
		GL11.glDisable(GL11.GL_POINT_SMOOTH);
		
		if (previousSlide) {
			if (state == 0) {
				currentSlideScale = initialScale;
				currentSlideRotation = 0.0;
				currentSlide--;
			}
			if (state < delta) {
				speed = step*(1-Math.sqrt(state/delta));
				state++;
			} else {
				speed = 0;
				state = 0;
				previousSlide = false;
			}
		}
		else if (nextSlide) {
			if (state == 0) {
				currentSlideScale = initialScale;
				currentSlideRotation = 0.0;
			}
			if (state < delta) {
				speed = step*Math.sqrt(state/delta);
				state++;
			} else {
				currentSlide++;
				speed = 0;
				state = 0;
				nextSlide = false;
			}
		}

	}

	@Override
	public void multiPressed(int userID, int leftx, int lefty, int rightx, int righty) {
		
		isMultiOn = true;
		
		if (last_left_xpos == -1) {
			// First frame pressed situation
			last_left_xpos = leftx;
			last_left_ypos = lefty;
			last_right_xpos = rightx;
			last_right_ypos = righty;

		} else {
			// Already pressed in last frame situation
			Vector2f lastvec = new Vector2f(last_left_xpos - last_right_xpos, last_left_ypos - last_right_ypos);
			Vector2f currentvec = new Vector2f(leftx - rightx, lefty - righty);

			
			// Scaling
			float coef = 0.15f;
			float current_length = currentvec.length();
			float last_length = lastvec.length();
			
			if (current_length < last_length - 2) {
				// Smaller
				currentSlideScale -= current_length/last_length*coef;
				if (currentSlideScale < 3) {
					currentSlideScale = 3;
				}

				last_left_xpos = leftx;
				last_left_ypos = lefty;
				last_right_xpos = rightx;
				last_right_ypos = righty;
			} else if (current_length > last_length + 2){
				// Bigger
				currentSlideScale += current_length/last_length*coef;
				if (currentSlideScale > 10) {
					currentSlideScale = 10;
				}

				last_left_xpos = leftx;
				last_left_ypos = lefty;
				last_right_xpos = rightx;
				last_right_ypos = righty;
			}
			
			// Rotation
			Vector2f horiz = new Vector2f(-1, 0);
			
			double angle = -Math.toDegrees(
					Math.atan2(horiz.x, horiz.y)
					- Math.atan2(currentvec.x/current_length, currentvec.y/current_length));
			
			if (Math.abs(angle) < 5) {
				currentSlideRotation = 0;
			} else if (Math.abs(currentSlideRotation - angle) > 4) {
				currentSlideRotation = angle;
			}
			
		}
	}


	@Override
	public void multiReleased(int userID, int leftx, int lefty, int rightx, int righty) {
//		currentSlideScale = initialScale;
//		last_left_xpos = -1;
		isMultiOn = false;
	}



	@Override
	public void mouseLeftClicked(int userID, int x, int y) {
		if (!isMultiOn) {
			if (left_xpos == -1) {
				left_xpos = x;
			}
		}
	}
	
	@Override
	public void mouseLeftReleased(int userID, int posx, int posy) {
		if (!isMultiOn) {
			if (posx - left_xpos < -TOLERANCE) {
				// if hand went left
				nextSlide();
				currentSlideRotation = 0.0;
				currentSlideScale = initialScale;
			}
			if (posx - left_xpos > TOLERANCE){
				// if hand went right
				previousSlide();
				currentSlideRotation = 0.0;
				currentSlideScale = initialScale;
			}
			left_xpos = -1;
		}
	}

	@Override
	public void mouseRightClicked(int userID, int x, int y) {
		if (!isMultiOn) {
			if (right_xpos == -1) {
				right_xpos = x;
			}	
		}
	}
	
	@Override
	public void mouseRightReleased(int userID, int posx, int posy) {
		if (!isMultiOn) {
			if (posx - right_xpos < -TOLERANCE) {
				// if hand went left
				nextSlide();
				currentSlideRotation = 0.0;
				currentSlideScale = initialScale;
			} 
			
			if (posx - right_xpos > TOLERANCE){
				// if hand went right
				previousSlide();
				currentSlideRotation = 0.0;
				currentSlideScale = initialScale;
			}
			right_xpos = -1;
		}
	}

	@Override
	public void pollInput(String key) {
		if (key.equals("KEY_RIGHT")) {
			nextSlide();
		} 
		else if (key.equals("KEY_LEFT")) {
			previousSlide();
		}
		else if (key.equals("KEY_R")) {
			currentSlideScale = initialScale;
			currentSlideRotation = 0.0;
		}
		else if (key.equals("KEY_A")) {
			setAvatarVisible(!avatarIsDrawn());
		}
		else if (key.equals("KEY_S")) {
			setSkeletonVisible(!skeletonIsDrawn());
		}
	}

}
