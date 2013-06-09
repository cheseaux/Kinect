package box2d;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;

import algorithms.Triangle;
import algorithms.Triangulator;

/**
 * Cette classe représente le lien avec le moteur physique de JBox2D.
 * Il comporte un ensemble de Body, gère les collisions entre eux et applique
 * les effets physique. Elle permet également d'ajouter des poylgones (convexes ou non),
 * des balles, etc.. dans le monde physique.
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class PhysicsWorld extends Thread {

	/** Fréquence de rafraîchissage du monde */
	private final float TIME_STEP = 10.0f/60.0f;
	
	/** Nombre d'itération pour les calculs de la vitesse */
	private final int VELOCITY_ITERATION = 5;
	
	/** Nombre max d'itérations pour les calculs de position */
	private final int POSITION_ITERATION = 6;
	
	/** Ratio entre mètres et pixel */
	public final static float PTM_RATIO = 1/50f;

	/** Liste des Body ajouté à ce monde */
	private ArrayList<Body> bodies = new ArrayList<Body>();

	/** Monde physique de JBox2D */
	private World world; 

	/**
	 * Initialise le monde physique
	 */
	public void createWorld() {
		Vec2 gravity = new Vec2((float) 0.0, (float) -10.0 * PTM_RATIO);  
		boolean doSleep = true; 
		world = new World(gravity, doSleep);
	}

	public synchronized Joint getJoint(JointDef jointDef) {
		return world.createJoint(jointDef);
	}

	/**
	 * Test si un point donné est à l'intérieur d'un objet physique
	 * @param body le body testé
	 * @param x l'abscisse du point
	 * @param y l'ordonnée du point
	 * @return true si le point est à l'intérieur, false sinon
	 */
	public boolean isInsideBody(Body body, int x, int y) {
		Fixture fixture = body.getFixtureList();
		Vec2 pointToTest = new Vec2(x * PTM_RATIO, y * PTM_RATIO);
		do {
			if (fixture.getShape().testPoint(body.getTransform(), pointToTest)) {
				return true;
			}
		} while ((fixture = fixture.m_next) != null);

		return false;
	}

	/**
	 * Construit et ajoute le body au monde physique
	 * @param bodyDef, la définition du Body
	 * @return le body ainsi construits
	 */
	public synchronized Body buildBody(BodyDef bodyDef) {
		//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return new Body(bodyDef, world);
	}

	/**
	 * Ajoute un objet statique dans le monde (mur)
	 * @param posX, abscisse de sa position
	 * @param posY, ordonnée de sa position
	 * @param width, longueur du mur
	 * @param height, largeur du mur
	 * @return le mur ainsi crée
	 */
	public synchronized Body addWall(float posX, float posY, float width, float height){
		//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		posX *= PTM_RATIO;
		posY *= PTM_RATIO;
		width *= PTM_RATIO;
		height *= PTM_RATIO;

		//create shape
		PolygonShape ps = new PolygonShape();
		ps.setAsBox(width,height);

		//create fixture and bing the shape to it
		FixtureDef fd = new FixtureDef();
		fd.shape = ps;
		fd.density = 1.0f;
		fd.friction = 0.7f;

		Filter filter = new Filter();
		filter.groupIndex = 0x0001;
		filter.categoryBits = 0x0001;
		filter.maskBits = 0x0001;
		fd.filter = filter;

		//create the body and bind the fixture to it
		BodyDef bd = new BodyDef();
		bd.position.set(posX, posY);
		Body body = world.createBody(bd);
		body.createFixture(fd);
		body.setUserData(new BodyData("", new Color(255,255,255), -1));
		bodies.add(body);
		return body;
	}

	/**
	 * Ajoute une balle au monde physique
	 * @param radius, le rayon de la balle
	 * @param x, l'abscisse
	 * @param y, l'ordonnée
	 * @param velocity, la vitesse intiale
	 * @param color, la couleur de la balle
	 * @return la balle ainsi crée
	 */
	public synchronized Body addBall(float radius, float x, float y, Vec2 velocity, Color color) {
		//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		return addBall(radius, x, y, velocity, color, "", -1);
	}

	/**
	 * Ajoute une balle au monde physique
	 * @param radius, le rayon de la balle
	 * @param x, l'abscisse
	 * @param y, l'ordonnée
	 * @param velocity, la vitesse intiale
	 * @param color, la couleur de la balle
	 * @param textureID, l'index de la texture de cette balle
	 * @return la balle ainsi crée
	 */
	public synchronized Body addBall(float radius, float x, float y, Vec2 velocity, Color color, String userData, int textureID) {  
		//		TextDisplay.println(Thread.currentThread().getStackTrace()[1].getMethodName());
		radius *= PTM_RATIO;
		x *= PTM_RATIO;
		y *= PTM_RATIO;

		//create body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC; // set body as Dynamic so that it response to forces
		bodyDef.position.set(x, y);
		//create body from definition
		Body body = world.createBody(bodyDef);
		//create shape
		CircleShape dynamicBall = new CircleShape();
		dynamicBall.m_radius = radius;
		//bind shape to body with fixture
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicBall;
		fixtureDef.density = 0.4f;
		fixtureDef.friction = 0.5f;
		fixtureDef.restitution = 0.85f;

		Filter filter = new Filter();
		filter.groupIndex = 0x0001;
		filter.categoryBits = 0x0001;
		filter.maskBits = 0x0001;
		fixtureDef.filter = filter;

		body.createFixture(fixtureDef);
		body.setLinearVelocity(velocity);
		body.setUserData(new BodyData(userData, color, textureID));

		bodies.add(body);

		return body;
	}  


	/**
	 * Ajoute un polygone au monde physique
	 * @param polygon, le polygone à ajouter
	 * @param x, l'absisse du polygone
	 * @param y, l'ordonnée du polygone
	 * @param color, sa couleur
	 * @param info, son type
	 * @param textureID, le numéro de Texture
	 * @return le polygon ainsi crée
	 */
	public synchronized Body addPolygon(Polygon polygon, float x, float y, Color color, String info, int textureID) {
		x *= PTM_RATIO;
		y *= PTM_RATIO;

		//create body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x,y);

		//Body from definition
		Body body = world.createBody(bodyDef);

		//create shape
		//		TextDisplay.println("Body Created successfully");
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(getPolygonVertices(polygon), polygon.npoints);
		//bind shape to body with fixture

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.3f;

		Filter filter = new Filter();
		filter.groupIndex = 0x0001;
		filter.categoryBits = 0x0001;
		filter.maskBits = 0x0004;
		fixtureDef.filter = filter;

		float randAngle = (float)(Math.round(4*Math.random()) * Math.PI);
		body.setTransform(new Vec2(x,y), randAngle);

		body.createFixture(fixtureDef);
		body.setUserData(new BodyData(info, color, textureID));
		bodies.add(body);
		
		return body;
	}

	/**
	 * Cette méthode permet de pallier au problème des formes non-convexes dans Box2D.
	 * Elle triangule le poylgon concave et ajoute une fixture par triangle au body.
	 * De cette manière les collisions et la physique sont gérés correctement.
	 * @param polygon, le polygon concave
	 * @param x, son abscisse
	 * @param y, son ordonnée,
	 * @param color, sa couleur
	 * @param textureID, sa texture
	 * @param info, son type
	 */
	public void addNonConvexPolygon(Polygon polygon, float x, float y, Color color, int textureID, String info) {
		Triangle[] triangles = Triangulator.triangulatePolygon(polygon);
		if (triangles == null) {
			triangles = Triangulator.triangulatePolygon(Triangulator.invertClockOrder(polygon));
			if (triangles == null)  {
				System.err.println("Impossible d'ajouter le polygon non-convexe");
				return;
			}
		}
		x *= PTM_RATIO;
		y *= PTM_RATIO;

		//create body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x,y);

		//Body from definition
		//		TextDisplay.println("Body creation");
		Body body = world.createBody(bodyDef);
		body.setUserData(new BodyData(info, color, textureID));

		//create shapes
		for (Triangle t : triangles) {

			PolygonShape polygonShape = new PolygonShape();

			polygonShape.set(t.getScaledVertices(), t.getScaledVertices().length);

			//bind shape to body with fixture
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			fixtureDef.density = 1.0f;
			fixtureDef.friction = 0.7f;
			fixtureDef.restitution = 0.3f;

			Filter filter = new Filter();
			filter.groupIndex = 0x0001;
			filter.categoryBits = 0x0001;
			filter.maskBits = 0x0004;
			fixtureDef.filter = filter;

			body.createFixture(fixtureDef);
		}

		bodies.add(body);
	}

	/**
	 * Add a triangle to the physics world. Be careful that the x and y positions are
	 * already in the physics world coordinates (multiply by PTM_RATIO)
	 * 
	 * @param triangle : triangle vertices
	 * @param x	: horizontal location in world coordinates
	 * @param y	: vertical location in world coordinates
	 * @param color : triangle color
	 */
	public void addTriangle(Triangle triangle, float x, float y, Color color) {
		//create body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x,y);

		//Body from definition
		Body body = world.createBody(bodyDef);

		//create shape
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(triangle.getVertices(), 3);

		//bind shape to body with fixture
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.3f;

		Filter filter = new Filter();
		filter.groupIndex = 0x0001;
		filter.categoryBits = 0x0001;
		filter.maskBits = 0x0004;
		fixtureDef.filter = filter;

		body.createFixture(fixtureDef);

		bodies.add(body);
		body.setUserData(new BodyData("", color, -1));
	}


	/**
	 * Retourne un tableau formés par les vertices du polygon
	 * @param polygon, le polygon dont on extrait les vertices
	 * @return un tableau de Vec2
	 */
	public Vec2[] getPolygonVertices(Polygon polygon) {
		Vec2[] result = new Vec2[polygon.npoints];
		for (int i = 0; i < polygon.npoints; i++) {
			result[i] = new Vec2(polygon.xpoints[i], polygon.ypoints[i]).mul(PTM_RATIO);
		}
		return result;
	}

	/**
	 * Invoque une itération sur le monde physique
	 */
	public synchronized void update() {  
		// Update Physics World
		world.step(TIME_STEP, VELOCITY_ITERATION, POSITION_ITERATION);

	}

	public synchronized ArrayList<Body> getBodies() {
		return new ArrayList<Body>(bodies);
	}

	/**
	 * Détruit et ôte le joint du monde physique
	 * @param mouseJoint
	 */
	public synchronized void destroyJoint(Joint mouseJoint) {
		world.destroyJoint(mouseJoint);

	}

	/**
	 * Supprime tous les Body qui ne sont plus utilisés
	 */
	public void clearDestroyedBodies() {
		for (int i = 0; i < bodies.size(); i++) {
			Body body = bodies.get(i);
			if (body.getUserData().isDestroyed()) {
				world.destroyBody(body);
				bodies.remove(body);
			}
		}
	}

} 