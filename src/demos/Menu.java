package demos;

import gestures.KinectMouseListener;
import gl.GLParameters;
import gl.Shader;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import kinect.KinectModule;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.jbox2d.common.Vec2;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import text.TextDisplay;

/**
 * Cette classe implémente le menu qui permet de passer entre chaque démonstration
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class Menu extends Observable implements KinectMouseListener, Observer{

	boolean test = true;
	
	private final int itemNumber = 6;
	
	private boolean active = true;
	
	// Array of menu items
	private ArrayList<MenuItem> items;
	
	// Mouse handling parameters
	private ArrayList<Rectangle> rect;
	
	private KinectModule kinectModule;
	
	private DemonstrationsCommon common;
	
	// Shaders
	private Shader shaderTexture;
	private static final String vertexShaderLocation = "shaders/tex.vs";
	private static final String fragmentShaderLocation = "shaders/tex.fs";
	
	// Desgin parameters
	private final int height = Display.getHeight(); // (600)
	private final int width = Display.getWidth(); // (800)
	private final int scale = Display.getWidth()/27; // (30)
	private final int dockHeight = Display.getHeight()/6;	// dock height (90)
	private final int dockD = Display.getWidth()/27; // dock trapeze coefficient (30)
	private int trans;
	private final float minScale = Display.getWidth()/30;
	private final float maxScale = Display.getWidth()/15;
	private ArrayList<Double> scales; 
	
	// Transition parameters
	private boolean show;
	private boolean hide;
	private double state;
	private boolean isVisible = false;
	private long timer;
	private final long restDuration = 2; // rest duration in sec
	private final float heightTreshold = height - dockHeight*2/3;


	/**
	 * Construction du menu et de tous les sous-menus
	 */
	public Menu(){
		
		show = false;
		hide = false;
		state = 0;
		
		// init kinect related features
		kinectModule = KinectModule.getInstance();
		common = DemonstrationsCommon.getInstance();
		common.subscribeToHandGestures(this);
		
		// init shader
		shaderTexture = new Shader(vertexShaderLocation, fragmentShaderLocation);
		
		// init menu items
		items = new ArrayList<MenuItem>(itemNumber);
		items.add(new MenuItem("data/menu/slides.png"));
		items.add(new MenuItem("data/menu/physics.png"));
		items.add(new MenuItem("data/menu/physics2.png"));
		items.add(new MenuItem("data/menu/physics3.png"));
		items.add(new MenuItem("data/menu/kinect_depth.png"));
		items.add(new MenuItem("data/menu/cameras.png"));
		
		scales = new ArrayList<Double>(itemNumber);
		rect = new ArrayList<Rectangle>(itemNumber);
		trans = (width-2*dockD)/(itemNumber+1);
		for (int i = 0; i < itemNumber; i++) {
			int xtrans = dockD + (i+1)*trans;
			int ytrans = height-dockHeight/2-2*scale;
			Rectangle rec = new Rectangle(xtrans-scale, ytrans+scale, (int)2.2*scale, (int)2.2*scale);
			rect.add(rec);
			scales.add(new Double(scale));
		}
	}


	/**
	 * Modifie la visiblité du menu (transition)
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		if (!show && !hide) {
			if (visible && !isVisible) {
				setChanged();
				notifyObservers(new Boolean(false));
				timer = System.currentTimeMillis();
				show = true;
				state = 0;
				isVisible = true;
			} else if (!visible && isVisible) {
				state = 0;
				hide = true;
			}
		}
	}

	/**
	 * Menu Projection:
	 * 
	 * Orthogonal projection : [0 ; Display.width] x [0 ; Display.height]
	 * 
	 */
	public void setProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	/**
	 * Dessine le menu
	 */
	public void draw(){
		handleGestures();
		
		
		if (isVisible) {
			setProjection();

			// enable alpha blending
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			double duration = 0.5;	// transition duration
			double delta = GLParameters.fps * duration;

			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, -0.1);
			if (show) {
				if (state <= delta) {
					GL11.glTranslated(0, dockHeight*(1-Math.sqrt(state/delta)), 0);
					state++;
				} else {
					state = 0;
					show = false;
				}
			}
			else if (hide) {
				if (state <= delta) {
					GL11.glTranslated(0, dockHeight*Math.sqrt(state/delta), 0);
					state++;
				} else {
					GL11.glScaled(0, 0, 0);
					state = 0;
					hide = false;
					isVisible = false;
					setChanged();
					notifyObservers(new Boolean(true));
				}
			}

			drawDock();

			try {
				shaderTexture.bind();
				drawItems();
				drawItemsReflection();
				shaderTexture.unbind();
			} catch (Exception e) {
				e.printStackTrace();
			}
			GL11.glPopMatrix();
			
			GL11.glDisable(GL11.GL_BLEND);
		}
	}

	/**
	 * Dessine la dock du menu
	 */
	private void drawDock(){
		
		GL11.glBegin(GL11.GL_POLYGON);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.4f);
		GL11.glNormal3d(1, 0, -1);
		GL11.glVertex3d(0, height, 0);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.7f);
		GL11.glNormal3d(1, 0, -1);
		GL11.glVertex3d(dockD, height, 0);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.7f);
		GL11.glNormal3d(1, 0, -4);
		GL11.glVertex3d(width-dockD, height, 0);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.4f);
		GL11.glNormal3d(0, 1, -4);
		GL11.glVertex3d(width, height, 0);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.0f);
		GL11.glNormal3d(-1, 0, -4);
		GL11.glVertex3d(width-dockD, height-dockHeight, 0);

		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.0f);
		GL11.glNormal3d(-1, 0, -1);
		GL11.glVertex3d(dockD, height-dockHeight, 0);

		GL11.glEnd();

	}


	private void drawItems() throws Exception {
		// Set shader parameter
		shaderTexture.setFloatUniform("reflection", 0.0f);
		
		int trans = (width-2*dockD)/(items.size()+1);

		for (int i = 0; i < items.size(); i++) {
			shaderTexture.setFloatUniform("saturation", new Float((scales.get(i)-minScale)/maxScale));
			
			// draw item
			GL11.glPushMatrix();
			GL11.glTranslated(dockD + (i+1)*trans, height-dockHeight/3, 0.2);
			GL11.glScaled(scales.get(i), scales.get(i), 1);
			items.get(i).draw();
			GL11.glPopMatrix();
		}
		
	}
	
	private void drawItemsReflection() throws Exception {
		// Set shader parameter
		shaderTexture.setFloatUniform("reflection", 1.0f);
		int trans = (width-2*dockD)/(items.size()+1);

		for (int i = 0; i < items.size(); i++) {
			shaderTexture.setFloatUniform("saturation", new Float((scales.get(i)-minScale)/maxScale));
			
			// draw reflection
			GL11.glPushMatrix();
			GL11.glTranslated(dockD + (i+1)*trans - 1, height-dockHeight/3-scales.get(i), 0.2);
			GL11.glScaled(scales.get(i), -scales.get(i)*4/5, 1);
			items.get(i).drawReflection();
			GL11.glPopMatrix();
		}
		
	}

	
	private void handleGestures() {
		if (!active) {
			return;
		}
		
		for (int userID : kinectModule.getUsers())  {
			if (kinectModule.isSkeletonReady(userID)) {
				// Get user left hand position
				SkeletonJointPosition leftHand = kinectModule.getJointsMap(userID).get(SkeletonJoint.LEFT_HAND);
				Vec2 leftPos = new Vec2(changeOfVariable(leftHand.getPosition().getX(), leftHand.getPosition().getY()));
				float leftz = leftHand.getPosition().getZ();
				
				// Get user right hand position
				SkeletonJointPosition rightHand = kinectModule.getJointsMap(userID).get(SkeletonJoint.RIGHT_HAND);
				Vec2 rightPos = new Vec2(changeOfVariable(rightHand.getPosition().getX(), rightHand.getPosition().getY()));
				float rightz = rightHand.getPosition().getZ();
				
				// Get user head position
				SkeletonJointPosition head = kinectModule.getJointsMap(userID).get(SkeletonJoint.RIGHT_HAND);
				float headz = head.getPosition().getZ();
				
				
				handleTransition(leftPos.y, rightPos.y);
				
				
				if (leftPos.y > rightPos.y) {
					handleScaling(leftPos.x, leftPos.y);
				} else {
					handleScaling(rightPos.x, rightPos.y);
				}
				
				if (isVisible) {
					if (headz - leftz > 400 && leftPos.y > rightPos.y) {
						handleClick(leftPos.x, leftPos.y);
					}
					else if (headz - rightz > 400) {
						handleClick(rightPos.x, rightPos.y);
					}
				}
			}
		}
		
	}
	
	private void handleTransition(float lefty, float righty) {
		
		if (lefty > heightTreshold || righty > heightTreshold) {
			timer = System.currentTimeMillis();
		}

		if (isVisible) {
			if(System.currentTimeMillis() - timer > restDuration*1000 &&
					lefty < heightTreshold && righty < heightTreshold) {
				setVisible(false);
			}
		}
		else {
			if (lefty > heightTreshold && righty > heightTreshold) {
				setVisible(true);
			}
		}
	}
	
	
	private void handleClick(float x, float y) {
		for (int i = 0; i < items.size(); i++) {
			if (rect.get(i).contains(x, y)) {
				setChanged();
				notifyObservers(i);
				items.get(i).setColor(1.0f, 0.0f, 0.0f);
			} else {
				items.get(i).setColor(1.0f, 1.0f, 1.0f);
			}
		}
	}
	
	
	private void handleScaling(float handx, float handy) {
		for (int i = 0; i < items.size(); i++) {
			float x = rect.get(i).x + rect.get(i).width/2;
			float y = rect.get(i).y - rect.get(i).height/2;
			
			double distance = Math.sqrt((x-handx)*(x-handx) + (y-handy)*(y-handy));
			double size = 2.4*scale*(1-Math.sqrt(distance*2/width));
			
			scales.add(i, Math.max(minScale, Math.min(maxScale, size)));
		}
	}
	
	
	
	private Vec2 changeOfVariable(float x, float y) {
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;

		return new Vec2(x, y);
	}
	

	
	@Override
	public void mouseLeftClicked(int userID, int x, int y) {
	}
	
	@Override
	public void mouseRightClicked(int userID, int x, int y) {
	}



	@Override
	public void mouseLeftPressed(int userID, int posx, int posy) {
	}


	
	@Override
	public void mouseRightPressed(int userID, int posx, int posy) {
	}

	
	

	@Override
	public void mouseLeftReleased(int userID, int posx, int posy) {
	}
	
	
	

	@Override
	public void mouseRightReleased(int userID, int posx, int posy) {
	}



	@Override
	public void multiPressed(int userID, int leftx, int lefty, int rightx,
			int righty) {
		
	}



	@Override
	public void multiReleased(int userID, int leftx, int lefty, int rightx,
			int righty) {
		
	}



	@Override
	public void update(Observable caller, Object arg) {
		if (caller instanceof Demonstrations && arg instanceof Boolean) {
			active = (Boolean) arg;
			TextDisplay.println("Menu is now " + (active ? "active" : "inactive") + " !");
		}
	}
	
}
