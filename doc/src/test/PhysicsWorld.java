package test;

import java.awt.Polygon;
import java.util.ArrayList;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class PhysicsWorld extends Thread {

	private final float TIME_STEP = 60.0f/1000.0f;
	private final int VELOCITY_ITERATION = 6;
	private final int POSITION_ITERATION = 2;

	private ArrayList<Body> bodies = new ArrayList<Body>();  

	private World world;  
	private BodyDef groundBodyDef;    

	public void createWorld() {
		// ====== Step 1: Create Physics World with Gravity ======
		Vec2 gravity = new Vec2((float) 0.0, (float) -10.0);  
		boolean doSleep = true;  
		world = new World(gravity, doSleep);
	}

	public void addWall(float posX, float posY, float width, float height){
		//create shape
		PolygonShape ps = new PolygonShape();
		ps.setAsBox(width,height);

		//create fixture and bing the shape to it
		FixtureDef fd = new FixtureDef();
		fd.shape = ps;
		fd.density = 1.0f;
		fd.friction = 0.3f;    

		//create the body and bind the fixture to it
		BodyDef bd = new BodyDef();
		bd.position.set(posX, posY);
		Body body = world.createBody(bd);
		body.createFixture(fd);
		bodies.add(body);
	}

	public void addBall(float radius, float x, float y, Vec2 velocity) {  
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
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.6f;
		body.createFixture(fixtureDef);
		body.setLinearVelocity(velocity);
		bodies.add(body);
	}  
	
	public void addPolygon(Polygon polygon, int x, int y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x,y);
		
		//Body from definition
		Body body = world.createBody(bodyDef);
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(getPolygonVertices(polygon), polygon.npoints);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.6f;
		body.createFixture(fixtureDef);
		bodies.add(body);
	}

	private Vec2[] getPolygonVertices(Polygon polygon) {
		Vec2[] result = new Vec2[polygon.npoints];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Vec2(polygon.xpoints[i], polygon.ypoints[i]);
		}
		return result;
	}

	public void update() {  
		// Update Physics World  
		world.step(TIME_STEP, VELOCITY_ITERATION, POSITION_ITERATION);
	}
	
	public ArrayList<Body> getBodies() {
		return bodies;
	}
	
} 