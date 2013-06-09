
package demos;

import gestures.UserHandPainter;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import kinect.KinectModule;

import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import text.TextDisplay;
import algorithms.PolygonSimplificationAlgorithm;
import box2d.BodyData;
import box2d.PhysicsWorld;

/**
 * Cette démonstration présente le moteur physique de JBox2D :
 * - Collisions d'objets
 * - Interactions avec l'utilisateur (sélection d'objets, dessin de formes, coup de pieds/tête etc..)
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class PhysicsDemonstration extends Demonstrations {
	
	private int count = 0;

	// Box2D parameters
	private PhysicsWorld world;

	/** Algorithme de simplification de polygons */
	private PolygonSimplificationAlgorithm polyAlgo = new PolygonSimplificationAlgorithm(12);
	
	/** Nombre de vertices par cercles */
	private final int CIRCLE_VERTICES_NUMBER = 60;
	
	/** Texturing */
	private Texture rectTexture = null;
	private int RECT_METAL = 100;
	private final int texBoxNumber = 5;
	private ArrayList<Texture> boxTexture = new ArrayList<Texture>(texBoxNumber);
	private final int TEX_BOX_WOOD = 3;
	private final int texBallNumber = 3;
	private ArrayList<Texture> ballTexture = new ArrayList<Texture>(texBallNumber);
	
	// Gestures parameters
	private HashMap<Integer, MouseJoint> userLeftJoint = new HashMap<Integer, MouseJoint>();
	private HashMap<Integer, MouseJoint> userRightJoint = new HashMap<Integer, MouseJoint>();
	
	// Skeleton parameters
	private HashMap<Integer, Body[]> userSkeletonBody = new HashMap<Integer, Body[]>();
	private HashMap<Integer, MouseJoint[]> userSkeletonJoint = new HashMap<Integer, MouseJoint[]>();
	
	public PhysicsDemonstration(DemoType type) {
		super(type);
		KinectModule.getInstance().addEventObserver(new LostUserObserver());
		setCursorVisible(false);
		setSkeletonVisible(true);
		loadTexture();
		initWorld();
		initScene();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		setSkeletonVisible(true);
		setAvatarVisible(false);
	}

	/**
	 * Chargement des textures
	 */
	private void loadTexture() {
		try {
			for (int i = 0; i < texBoxNumber; i++) {
				
			}
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/box_metal.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/box_metal2.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/box_metal3.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/box_wood.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/box_wood2.png")));
			
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/brick1.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/brick2.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/brick3.png")));
			boxTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/brick4.png")));
			
			ballTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/ball1.png")));
			ballTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/ball2.png")));
			ballTexture.add(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/ball3.png")));
			
			rectTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("data/physics/rect_metal.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise le monde physique
	 */
	private void initWorld() {
		world = new PhysicsWorld();  
		world.createWorld();

		addWall(0.0f, 0.0f, 8*Display.getWidth(), 1.0f);
		addWall(Display.getWidth(), 0.0f, 1.0f, 8*Display.getHeight());
		addWall(0.0f, 0.0f, 1.0f, 8*Display.getHeight());
		addWall(0.0f, 4*Display.getHeight(), 1.0f, 8*Display.getHeight());
		
		world.start();
	}
	
	/**
	 * Initialise la scène (placement d'objets)
	 */
	private void initScene() {
	
		if(getDemoType().equals(DemoType.PHYSICS1)) {
			int scale = 70;
			addPolygon(getSquarePolygon(100), 110, 100, Color.WHITE, TEX_BOX_WOOD);
			addPolygon(getRectangle((int)(2.7*scale), scale), (int)(2.7*scale/2), 200, Color.WHITE, RECT_METAL);
			addPolygon(getRectangle((int)(2.7*scale), scale), (int)(2.7*scale/2), (int)(1.5*scale) + 200, Color.WHITE, RECT_METAL);
			
			scale = 15;
			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < 8; j++) {
					int x = Display.getWidth()-1 - (j+1)*2*scale;
					int y = scale*i;
					if (i%2 == 1) {
						x += scale;
					}
					Body brick = addPolygon(getRectangle(2*scale, scale), x, y, Color.WHITE, "brick", (int)(Math.floor(Math.random()*4 + 5)));
					brick.m_mass = 20;
				}
			}
			
			scale = 50;
			for (int i = 0; i < 3; i++) {
				addBall(scale, 300 + scale*i, 300 + scale*i, new Vec2(0,0), Color.WHITE, i%texBallNumber);
			}
		} 
		
		else if(getDemoType().equals(DemoType.PHYSICS2)) {
			int scale = 40;
			int n = (int)(Display.getHeight() / (2.5*scale));
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < i; j++) {
					float x = scale+10 + (n-i)*scale + j*2*scale;
					float y = (n-i)*2*scale;
					addPolygon(getSquarePolygon(scale), x, y, Color.WHITE);
				}
			}
			
			addBall(55, Display.getWidth() - 60, 500, new Vec2(0,0), Color.WHITE, 0);
			
		} 
		
		else if(getDemoType().equals(DemoType.PHYSICS3)) {
			for (int i = 0; i < 3; i++) {
				float xpos = (float)Math.random()*Display.getWidth();
				float ypos = (float)Math.random()*Display.getHeight();
				int scale = (int)(Math.random()*40 + 30);
				addBall(scale, xpos, ypos, new Vec2(0,0), Color.WHITE,1);
			}
		}
	}
	
	/** Génère une couleur aléatoire */
	private Color getRandomColor() {
		int r = (int)(255*Math.random());
		int g = (int)(255*Math.random());
		int b = (int)(255*Math.random());
		return new Color(r, g, b);
	}
	
	
	/**
	 * 
	 * @param scale : the half size of an edge
	 * @return
	 */
	private Polygon getSquarePolygon(int scale) {
		Polygon poly = new Polygon();
		poly.addPoint(scale,scale);
		poly.addPoint(-scale,scale);
		poly.addPoint(-scale,-scale);
		poly.addPoint(scale,-scale);
		return poly;
	}

	private Polygon getRectangle(int width, int height) {
		Polygon rect = new Polygon();
		rect.addPoint(width/2, height/2);
		rect.addPoint(-width/2, height/2);
		rect.addPoint(-width/2, -height/2);
		rect.addPoint(width/2, -height/2);
		return rect;
	}

	private synchronized void drawScene(){
		int bodiesCount = world.getBodies().size();
		for (int i = 0; i < bodiesCount; i++) {
			Body body = world.getBodies().get(i);
			draw(body);
		}
	}

	/**
	 * Méthode de dessin d'un Body 
	 * @param body le body à dessiner
	 */
	private void draw(Body body) {
		String type = body.getUserData().getType();
		if (type.equals("wall") || type.equals("bone")) {
			return;
		}
		
		for (UserHandPainter painter : kinectModule.getHandPainters().values()) {
			if (painter.isFetchLeft()) {
				drawPolygonContours(painter.getLeftPolygon(), Color.green);
			}
			if (painter.isFetchRight()) {
				drawPolygonContours(painter.getRightPolygon(), Color.green);
			}
		}

		GL11.glPushMatrix();
		GL11.glScaled(1/PhysicsWorld.PTM_RATIO, 1/PhysicsWorld.PTM_RATIO, 1);
		GL11.glTranslatef(body.getPosition().x, body.getPosition().y, 0);
		GL11.glRotatef((float)(body.getAngle() / Math.PI * 180), 0, 0, 1);
		
		Fixture fixture = body.getFixtureList();
		Color color = body.getUserData().getColor();
		int texIndex = body.getUserData().getTextureIndex();
		int colorShift = 0;
		do
		{
			if (body.getUserData().getType().equals("drawn")) {
				color = new Color((color.getRed() + colorShift) % 255, 
						(color.getGreen() + colorShift) % 255, 
						(color.getBlue() + colorShift) % 255);
			}
			colorShift = colorShift + 10;
			Shape shape = fixture.getShape();
			if (shape instanceof CircleShape) {
				drawCircle((CircleShape)shape, color, texIndex);
			} else if (shape instanceof PolygonShape) {
				if (((PolygonShape)shape).getVertexCount() == 4) {
					drawBox((PolygonShape)shape, color, texIndex);
				} else {
					drawPolygon((PolygonShape)shape, color);
				}
			}
		} while ((fixture = fixture.m_next) != null) ;
		
		GL11.glPopMatrix();
	}

	/**
	 * Render a box with 4 vertices
	 * 
	 * @param body is a Box2D world body with a polygon shape and 4 vertices
	 */
	private void drawBox(PolygonShape shape, Color color, int texIndex) {
		GL11.glColor3d(color.getRed(), color.getGreen(), color.getBlue());
		
		if (texIndex < 100) {
			boxTexture.get(texIndex).bind();
		} else {
			rectTexture.bind();
		}
		
		GL11.glBegin(GL11.GL_QUADS); 
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(shape.getVertex(0).x, shape.getVertex(0).y, 0);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(shape.getVertex(1).x, shape.getVertex(1).y, 0);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(shape.getVertex(2).x, shape.getVertex(2).y, 0);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(shape.getVertex(3).x, shape.getVertex(3).y, 0);
		GL11.glEnd();
	}
	

	/**
	 * Render a polygon shape with many vertices
	 * 
	 * @param body is a Box2d world body with a polygon shape
	 */
	private void drawPolygon(PolygonShape shape, Color color) {
		GL11.glColor3d(color.getRed() /255.0, color.getGreen() /255.0, color.getBlue()/255.0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, -1);
		GL11.glBegin(GL11.GL_POLYGON); 
		for (int i = 0; i < shape.getVertexCount(); i++) {
			GL11.glVertex2f(shape.getVertex(i).x, shape.getVertex(i).y);
		}
		GL11.glEnd();
		
	}

	/**
	 * Render a circle shape
	 * 
	 * @param body is a Box2D world body with a circle shape
	 */
	private void drawCircle(CircleShape shape, Color color, int textureID){
		GL11.glColor3d(color.getRed(), color.getGreen(), color.getBlue());
		
		ballTexture.get(textureID).bind();
		
		Disk d = new Disk();
		d.setDrawStyle(GLU.GLU_FILL);
		d.setTextureFlag(true);
		d.draw(0.0f, shape.m_radius, CIRCLE_VERTICES_NUMBER, CIRCLE_VERTICES_NUMBER);
	}

	/**
	 * Changement de variable entre coordonnées Box2D et coordonnées openGL
	 * @param x
	 * @param y
	 * @return
	 */
	private Vec2 changeOfVariable(float x, float y) {
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;

		return new Vec2(x, y);
	}

	/**
	 * Physics Projection
	 * Orhtogonal projection : [0 ; Display.width] x [0 ; Display.heigth]
	 */
	@Override
	public void setProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 100, -100);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

	}

	@Override
	public synchronized void draw() {
		try {
			handlePhysicsSkeleton();
		} catch (StatusException e) {
			e.printStackTrace();
		}
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		world.update();
		drawScene();
		world.clearDestroyedBodies();
		
		GL11.glDisable(GL11.GL_BLEND);
	}

	/**
	 * Rajoute des corps suivant les pieds et la tête, pour donner l'effet de coup
	 * de pied ou coup de tête sur les objets. Ces corps ne seront pas dessinés.
	 * @throws StatusException
	 */
	private void handlePhysicsSkeleton() throws StatusException {
		ArrayList<Integer> users = new ArrayList<Integer>();
		
		for (int userID : kinectModule.getUsers())  {
			users.add(userID);
			
			if (kinectModule.isSkeletonReady(userID)) {
				Body[] bodies = userSkeletonBody.get(new Integer(userID));


				Vec2 leftFoot = getBonePos(userID, SkeletonJoint.LEFT_FOOT);
				Vec2 rightFoot = getBonePos(userID, SkeletonJoint.RIGHT_FOOT);
				Vec2 head = getBonePos(userID, SkeletonJoint.HEAD);				

				// User has just arrived
				if (bodies == null) {
//					System.out.println("New Skeleton for user: " + userID);

					float radius = 30;
					
					// Create world bodies
					bodies = new Body[3];
					bodies[0] = addBone(radius, leftFoot.x, leftFoot.y, 1);
					bodies[1] = addBone(radius, rightFoot.x, rightFoot.y, 1);
					bodies[2] = addBone(radius, head.x, head.y, 1);
					userSkeletonBody.put(new Integer(userID), bodies);

//					System.out.println("Bodies: " + bodies[0] + ", " + bodies[1] + ", " + bodies[2]);

					// Create joints
					float jointForce = 50f;
					MouseJoint[] joints = new MouseJoint[3];
					joints[0] = createMouseJoint(bodies[0], (int)leftFoot.x, (int)leftFoot.y, jointForce);
					joints[0].setUserData(new BodyData("bone", Color.WHITE, -1));
					joints[1] = createMouseJoint(bodies[1], (int)rightFoot.x, (int)rightFoot.y, jointForce);
					joints[1].setUserData(new BodyData("bone", Color.WHITE, -1));
					joints[2] = createMouseJoint(bodies[2], (int)head.x, (int)head.y, jointForce);
					joints[2].setUserData(new BodyData("bone", Color.WHITE, -1));
					userSkeletonJoint.put(new Integer(userID), joints);
				}
				// User was already here
				else {
					leftFoot.mulLocal(PhysicsWorld.PTM_RATIO);
					rightFoot.mulLocal(PhysicsWorld.PTM_RATIO);
					head.mulLocal(PhysicsWorld.PTM_RATIO);
					// prevent from collide to avoid weird movements
					Filter prevent_filt = new Filter();
					prevent_filt.categoryBits = 0x0002;
					prevent_filt.maskBits = 0x0002;
					
					// allow feet to collide with bodies
					Filter allow_filt = new Filter();
					allow_filt.categoryBits = 0x0001;
					allow_filt.maskBits = 0x0001;
					
					if (isOutsideScreen(leftFoot.mul(1/PhysicsWorld.PTM_RATIO))) {
						bodies[0].getFixtureList().setFilterData(prevent_filt);
					} else {
						bodies[0].getFixtureList().setFilterData(allow_filt);
						
						// Update joints target
						MouseJoint[] joints = userSkeletonJoint.get(new Integer(userID));
						joints[0].setTarget(leftFoot);
					}
					
					if (isOutsideScreen(rightFoot.mul(1/PhysicsWorld.PTM_RATIO))) {
						bodies[1].getFixtureList().setFilterData(prevent_filt);
					} else {
						bodies[1].getFixtureList().setFilterData(allow_filt);
						
						// Update joints target
						MouseJoint[] joints = userSkeletonJoint.get(new Integer(userID));
						joints[1].setTarget(rightFoot);
					}
					
					if (isOutsideScreen(head.mul(1/PhysicsWorld.PTM_RATIO))) {
						preventBoneFromColliding(bodies[2]);
					} else {
						allowBoneToCollide(bodies[2]);
						
						// Update joints target
						MouseJoint[] joints = userSkeletonJoint.get(userID);
						joints[2].setTarget(head);
					}
					
					
				}
			}
		}
	}
	
	
	/**
	 * Get the bone position in the world coordinate. Assume that the joint is not null
	 * 
	 * @param userID
	 * @param joint
	 * @return
	 */
	private Vec2 getBonePos(int userID, SkeletonJoint joint) throws StatusException {
		// Get user joint position in kinect coordinates
		SkeletonJointPosition skelPos = kinectModule.getJointsMap(userID).get(joint);
		
		Vec2 jointPos = new Vec2(changeOfVariable(skelPos.getPosition().getX(), skelPos.getPosition().getY()));
		
		return jointPos;
	}
	
	
	/** 
	 * Prevent the bone from colliding if it is outside the screen
	 * 
	 * @param userID
	 * @param bodyIndex
	 */
	private void preventBoneFromColliding(Body body) {
		Filter filt = new Filter();
		filt.categoryBits = 0x0002;
		filt.maskBits = 0x0002;
		
		body.getFixtureList().setFilterData(filt);
	}
	
	/**
	 * Allow the bone to collide if it is back into the screen
	 * 
	 * @param userID
	 * @param bodyIndex
	 */
	private void allowBoneToCollide(Body body) {
		
		Filter filt = new Filter();
		filt.categoryBits = 0x0001;
		filt.maskBits = 0x0001;

		body.getFixtureList().setFilterData(filt);
	}
	
	/** Define if a 2D point is out of the Display */
	private boolean isOutsideScreen(Vec2 vec) {
		return vec.x < 0 || vec.x > Display.getWidth() || vec.y < 0 || vec.y > Display.getHeight();
	}
	
	@Override
	public synchronized  void mouseLeftClicked(int userID, int x, int y) {
		super.mouseLeftClicked(userID, x, y);
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;
		
		MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
		if  (leftJoint == null) {
			boolean hitDetected = false;
			for (Body body : world.getBodies()) {
				if (world.isInsideBody(body, x, y)) {
					hitDetected = true;
					leftJoint = createMouseJoint(body, x, y, 100f);
					userLeftJoint.put(new Integer(userID), leftJoint);
					break;
				}
			}
			if (!hitDetected) {
				UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
				if (painter != null) {
					painter.enableLeft(true);
				}
			}
		}
	}

	@Override
	public synchronized  void mouseRightClicked(int userID, int x, int y) {
		super.mouseRightClicked(userID, x, y);
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;
		
		MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
		if  (rightJoint == null) {
			boolean hitDetected = false;
			for (Body body : world.getBodies()) {
				if (world.isInsideBody(body, x, y)) {
					hitDetected = true;
					rightJoint = createMouseJoint(body, x, y, 100f);
					userRightJoint.put(new Integer(userID), rightJoint);
					TextDisplay.println("Joint ajouté ! Numéro " + userID);
					System.out.println("Joint droite mouse clicked = " + userRightJoint);
					break;
				}
			}
			if (!hitDetected) {
				UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
				if (painter != null) {
					painter.enableRight(true);
				}
			}
		}
	}

	@Override
	public synchronized void mouseLeftReleased(int userID, int posx, int posy) {
		super.mouseLeftReleased(userID, posx, posy);
		posx = posx*Display.getWidth()/640;
		posy = Display.getHeight() - posy*Display.getHeight()/480;

		MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
		if (leftJoint != null) {
			world.destroyJoint(leftJoint);
			userLeftJoint.put(new Integer(userID), null);
		} 
		UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
		if (painter.isFetchLeft()) {
			Polygon poly = polyAlgo.simplifyPolygon(painter.getLeftPolygon());
			
			Rectangle bounds = poly.getBounds();
			double dx = - bounds.getCenterX();
			double dy = - bounds.getCenterY();
			
			poly.translate((int) dx, (int) dy);
			
			if (poly.npoints > 2) {
				addNonConvexPolygon(poly, (float) posx, (float) posy, Color.RED, "drawn");
			}
			painter.resetLeft();
			painter.enableLeft(false);
		}
	}


	@Override
	public synchronized void mouseRightReleased(int userID, int posx, int posy) {
		super.mouseRightReleased(userID, posx, posy);
//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		posx = posx*Display.getWidth()/640;
		posy = Display.getHeight() - posy*Display.getHeight()/480;

		MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
		if (rightJoint != null) {
			world.destroyJoint(rightJoint);
			userRightJoint.put(new Integer(userID), null);
		} 
		UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
		if (painter.isFetchRight()) {
			Polygon poly = polyAlgo.simplifyPolygon(painter.getRightPolygon());
			Rectangle bounds = poly.getBounds();
			double dx = - bounds.getCenterX();
			double dy = - bounds.getCenterY();
			
			poly.translate((int) dx, (int) dy);
			
			if (poly.npoints > 2) {
				addNonConvexPolygon(poly, (float) posx, (float) posy, getRandomColor(), "drawn");
			}
			painter.resetRight();
			painter.enableRight(false);
			
		}
	}

	@Override
	public synchronized void mouseLeftPressed(int userID, int x, int y) {
		super.mouseLeftPressed(userID, x, y);
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;

		MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
		if (leftJoint != null) {
			Vec2 position = new Vec2(x,y).mul(PhysicsWorld.PTM_RATIO);
			leftJoint.setTarget(position);
		} else {
			//Draw with hand
			UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
			painter.updateLeft(x,y);
		}
	}

	@Override
	public synchronized void mouseRightPressed(int userID, int x, int y) {
		super.mouseRightPressed(userID, x, y);
		x = x*Display.getWidth()/640;
		y = Display.getHeight() - y*Display.getHeight()/480;

		MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
		System.out.println("GETTING userRightJoint for user " + userID);
		System.out.println("Right joint is = " + rightJoint);
		System.out.println("UserRIghtJoint = " + userRightJoint);
		if (rightJoint != null) {
			Vec2 position = new Vec2(x,y).mul(PhysicsWorld.PTM_RATIO);
			rightJoint.setTarget(position);
		} else {
			TextDisplay.println("Right joint is null :(");
			//Draw with hand
			UserHandPainter painter = kinectModule.getHandPainters().get(new Integer(userID));
			painter.updateRight(x,y);
		}

	}
	
	private void drawPolygonContours(Polygon polygon, Color color) {
		
		GL11.glColor3d(color.getRed(), color.getGreen(), color.getBlue());
		GL11.glBegin(GL11.GL_LINE_STRIP); 
		for (int i = 0; i < polygon.npoints; i++) {
			GL11.glVertex2f(polygon.xpoints[i], polygon.ypoints[i]);
		}
	
		GL11.glEnd();
	}

	@Override
	public synchronized void multiPressed(int userID, int leftx, int lefty, int rightx, int righty) {
		super.multiPressed(userID, leftx, lefty, rightx, righty);
		leftx = leftx*Display.getWidth()/640;
		lefty = Display.getHeight() - lefty*Display.getHeight()/480;
		rightx = rightx*Display.getWidth()/640;
		righty = Display.getHeight() - righty*Display.getHeight()/480;
		
		MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
		MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
		
		// Click case
		if (rightJoint == null || leftJoint == null) {
			for (Body body : world.getBodies()) {
				if (world.isInsideBody(body, leftx, lefty) && world.isInsideBody(body, rightx, righty)) {
					// create joints for both hands
					leftJoint = createMouseJoint(body, leftx, lefty, 100f);
					rightJoint = createMouseJoint(body, rightx, righty, 100f);
					// add joints to hashtables
					userLeftJoint.put(new Integer(userID), leftJoint);
					userRightJoint.put(new Integer(userID), rightJoint);
				}
			}
		}
		// Press case
		else if (rightJoint != null && leftJoint != null) {
			// Get coordinate in the right system
			Vec2 leftpos = new Vec2(leftx,lefty).mul(PhysicsWorld.PTM_RATIO);
			Vec2 rightpos = new Vec2(rightx,righty).mul(PhysicsWorld.PTM_RATIO);
			// Set joint target
			leftJoint.setTarget(leftpos);
			rightJoint.setTarget(rightpos);
		} 
	}

	@Override
	public synchronized void multiReleased(int userID, int leftx, int lefty, int rightx, int righty) {
		super.multiReleased(userID, leftx, lefty, rightx, righty);
		leftx = leftx*Display.getWidth()/640;
		lefty = Display.getHeight() - lefty*Display.getHeight()/480;
		rightx = rightx*Display.getWidth()/640;
		righty = Display.getHeight() - righty*Display.getHeight()/480;
		
		MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
		MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
		
		// if both joints are non null then an object is being carried
		if (rightJoint != null) {
			// destroy joint
			world.destroyJoint(rightJoint);
			userRightJoint.put(new Integer(userID), null);
			rightJoint = null;
		}
		
		if (leftJoint != null) {
			// destroy joint
			world.destroyJoint(leftJoint);
			userLeftJoint.put(new Integer(userID), null);
			leftJoint = null;			
		}
	}
	
	private MouseJoint createMouseJoint(Body body, int x, int y, float maxForceCoef) {
//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		MouseJointDef mouseDefJoint = new MouseJointDef();
		mouseDefJoint.bodyA = world.getBodies().get(0);
		//						mouseDefJoint.bodyA = body;
		mouseDefJoint.bodyB = body;
		mouseDefJoint.collideConnected = true;
		mouseDefJoint.target.set(x * PhysicsWorld.PTM_RATIO, y * PhysicsWorld.PTM_RATIO);
		mouseDefJoint.dampingRatio = 0.1f;
		mouseDefJoint.frequencyHz = 30.0f;
		mouseDefJoint.maxForce = maxForceCoef * body.getMass();
		return (MouseJoint) world.getJoint(mouseDefJoint);
	}
	
	/**
	 * Add a wall (a static body that does not move and has infinit mass) to the given position
	 * 
	 * 
	 * @param posX	wall horizontal position
	 * @param posY 	wall vertical position
	 * @param width	  wall width
	 * @param height  wall height
	 */
	private void addWall(float posX, float posY, float width, float height){
		Body wall = world.addWall(posX, posY, width, height);
		wall.setUserData(new BodyData("wall", Color.WHITE, -1));
	}
	

	/**
	 * Add a ball with a userData
	 * 
	 * @param radius 	ball radius
	 * @param x			ball horizontal position
	 * @param y			ball vertical position
	 * @param velocity	ball linear velocity
	 * @param color		ball color
	 * @param userData  used to indicate if this ball is a bone or a conventional body
	 */
	private Body addBall(float radius, float x, float y, Vec2 velocity, Color color, String userData, int textureID) {
		Body body = world.addBall(radius, x, y, velocity, color, userData, textureID);
		BodyData bodyData = body.getUserData();
		body.setUserData(new BodyData(bodyData.getType(), bodyData.getColor(), bodyData.getTextureIndex()));
		return body;
	}
	
	/**
	 * Add a ball to the physics world (without userData)
	 * 
	 * @param radius 	ball radius
	 * @param x			ball horizontal position
	 * @param y			ball vertical position
	 * @param velocity	ball linear velocity
	 * @param color		ball color
	 */
	private void addBall(float radius, float x, float y, Vec2 velocity, Color color, int textureID) { 
		this.addBall(radius, x, y, velocity, color, "", textureID);
	}
	
	
	
	/**
	 * Add a bone
	 * 
	 * @param radius 	bone radius
	 * @param x			bone horizontal position
	 * @param y			bone vertical position
	 */
	private Body addBone(float radius, float x, float y, int textureID) {
		Body body = world.addBall(radius, x, y, new Vec2(0,0), Color.WHITE, "bone", textureID);
		body.setFixedRotation(true);
		return body;
	}
	
	
	
	/**
	 * Add a polygon with a random texture
	 * 
	 * @param polygon 
	 * @param x		polygon horizontal position
	 * @param y		polygon vertical position
	 * @param color polygon color
	 */
	private void addPolygon(Polygon polygon, float x, float y, Color color) {
		world.addPolygon(polygon, x, y, color, "", (int)(texBoxNumber*Math.random()));
	}
	
	private void addNonConvexPolygon(Polygon polygon, float x, float y, Color color, String info) {
		//Don't know why, but coordinates close to 0
		//mess up Box2D step function...
		if (!isValid(polygon)) {
			return;
		}
		world.addNonConvexPolygon(polygon, x, y, color, 0, info);
	}
	

	/** A polygon is not valid if one of his coordinate is between [0, 1]
	 * We don't know why but it would generate an infinite loop in the Box2D library
	 * @param polygon
	 * @return
	 */
	private boolean isValid(Polygon polygon) {
		for (int i = 0; i < polygon.npoints; i++) {
			if (Math.abs(polygon.xpoints[i]) < 2 || Math.abs(polygon.ypoints[i]) < 2) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Add a polygon with a given texture
	 * 
	 * @param polygon
	 * @param x		polygon horizontal position
	 * @param y		polygon vertical position
	 * @param color polygon color
	 * @param tex	polygon texture
	 */
	private void addPolygon(Polygon polygon, float x, float y, Color color, int tex) {
		world.addPolygon(polygon, x, y, color, "", tex);
	}
	
	/**
	 * Add a polygon with a given texture
	 * 
	 * @param polygon
	 * @param x		polygon horizontal position
	 * @param y		polygon vertical position
	 * @param color polygon color
	 * @param info	
	 * @param tex	polygon texture
	 */
	private Body addPolygon(Polygon polygon, float x, float y, Color color, String info, int tex) {
		return world.addPolygon(polygon, x, y, color, info, tex);
	}

	class LostUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args)
		{
			int userID = args.getId();

			MouseJoint leftJoint = userLeftJoint.get(new Integer(userID));
			if (leftJoint != null) {
				world.destroyJoint(leftJoint);
				leftJoint = null;
			}
			
			MouseJoint rightJoint = userRightJoint.get(new Integer(userID));
			if (rightJoint != null) {
				world.destroyJoint(rightJoint);
				rightJoint = null;
			}
			
			userRightJoint.put(new Integer(userID), null);
			userLeftJoint.put(new Integer(userID), null);
			
			
			System.out.println("Remove physcis skeleton for user " + userID);
			
			Body[] bodies = userSkeletonBody.get(userID);
			MouseJoint[] joints = userSkeletonJoint.get(userID);
			
			// Remove joints
			if (joints != null) {
				for (int i = 0; i < joints.length; i++) {
					world.destroyJoint(joints[i]);
					
					System.out.println("remove joint " + joints[i]);
				}
			}

			userSkeletonJoint.put(userID, null);
			
			// Remove bodies from world
			if (bodies != null) {

				int length = bodies.length;
				for (int i = 0; i < length; i++) {
					Object obj = bodies[i].getUserData();
					if (obj instanceof BodyData) {
						BodyData bodyData = (BodyData) obj;
						bodyData.setDestroyed(true);
					} else {
						System.err.println("Body doesn't have any BodyData !");
					}
				}
			}
			userSkeletonBody.put(userID, null);
		}
	}

	@Override
	public void pollInput(String key) {
		if (key.equals("KEY_SPACE")) {
			addRandomBall();
			count++;
		}
		else if (key.equals("KEY_R")) {
			resetScene();
		}
		else if (key.equals("KEY_A")) {
			setAvatarVisible(!avatarIsDrawn());
		}
		else if (key.equals("KEY_S")) {
			setSkeletonVisible(!skeletonIsDrawn());
		}
	}
	
	private void addRandomBall() {
		float xpos = (float)Math.random()*Display.getWidth();
		float ypos = 1000;
		int scale = (int)(Math.random()*40 + 20);
		addBall(scale, xpos, ypos, new Vec2(0,0), Color.WHITE, 0);
	}
	
	private void resetScene() {
		ArrayList<Body> bodies = world.getBodies();
		int size = bodies.size();
		for (int i = 0; i < size; i++) {
			BodyData data = bodies.get(i).getUserData();
			if (data.getType().equals("drawn")) {
				data.setDestroyed(true);
			}
		}
	}

	public int getCount() {
		return count;
	}
	
	
}
