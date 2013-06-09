package draft;


import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import test.PhysicsWorld;

public class PainterGl {

	private PhysicsWorld world;
	private final int CIRCLE_VERTICES_NUMBER = 15;
	private float defaultRadius = 10.0f;

	public PainterGl() {
		world = new PhysicsWorld();  
		world.createWorld();

		// STEP 3 : create an obstacle
		world.addWall(0.0f, 0.0f, 1000.0f, 1.0f);
		world.addWall(800.0f, 0.0f, 1.0f, 1000.0f);
		world.addWall(0.0f, 0.0f, 1.0f, 1000.0f);
		world.start();

		initScene();
		initOpenGL();
		start();
	}

	private void initScene() {
		for (int i = 0; i < 100; i++) {
			world.addBall((float) Math.random() * defaultRadius,
					(float) (800.0f * Math.random()),
					(float) (600.0f * Math.random()),
					new Vec2(0.0f, - (float) Math.random() * 20.0f));
		}
	}

	public void initOpenGL() {
		try {
			Display.setDisplayMode(new DisplayMode(800,600));
			//			Display.setFullscreen(true);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// init OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		System.out.println("OpenGL initiated");
	}


	public void start() {

		long startTime = System.currentTimeMillis();
		int frameCount = 0;

		while (!Display.isCloseRequested()) {

			if (System.currentTimeMillis() - startTime >= 60.0/1000.0) {

				// Clear the screen and depth buffer
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	

				//Update Physics Wolrd
				world.update();

				//TimeStemp
				for (int i = 0; i < world.getBodies().size(); i++) {
					int j = i % 100;
					GL11.glColor3f(j*0.01f, j*0.01f, 1.0f);

					draw(world.getBodies().get(i));
				}
				pollInput();
				Display.sync(60);
				Display.update();
				//			System.out.println("Updating Display !");
				startTime = System.currentTimeMillis();
			}

		}


		Display.destroy();
	}

	public void pollInput() {
		try {
			Mouse.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Mouse.isButtonDown(0)) {
			int x = Mouse.getX();
			int y = Mouse.getY();
			world.addBall((float) Math.random() * defaultRadius, (float) x, (float) y, new Vec2());
			Mouse.destroy();
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			for (int i = 0; i < world.getBodies().size(); i++) {
				Body currentBody = world.getBodies().get(i);
				if (currentBody.getFixtureList().getShape() instanceof CircleShape) {
					world.getBodies().remove(i);
					currentBody.destroyFixture(currentBody.getFixtureList());
					currentBody = null;

				}
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			defaultRadius += 0.1f;
			System.out.println("Radius = " + defaultRadius);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			defaultRadius -= 0.1f;
			System.out.println("Radius = " + defaultRadius);
		}
	}

	private void draw(Body body) {
		Shape shape = body.getFixtureList().getShape();

		if (shape instanceof CircleShape) {
			drawCircle(body);
		} else if (shape instanceof PolygonShape) {
			drawPolygon((PolygonShape) shape);
		}

	}
	
	private void drawPolygon(Body body){
		PolygonShape shape = (PolygonShape) body.getFixtureList().getShape();
		int vertexCount = shape.getVertexCount();
		GL11.glBegin(GL11.GL_POLYGON);
		for (int i = 0; i < vertexCount; i++) {
			GL11.glVertex2f(shape.getVertex(i).x + body.getPosition().x, body.getPosition().y + shape.getVertex(i).y);

		}
		System.out.println("Drawing : " + body.getPosition().x + " - " + body.getPosition().y);
		GL11.glEnd();
	}
	
	

	private void drawCircle(Body body){

		Vec2 position = body.getPosition();
		float radius = body.getFixtureList().getShape().m_radius;


		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2f(position.x, position.y);
		float angle = 0.0f;
		for( angle = 0; angle < 2.0*Math.PI + 0.0001; ){
			float xpos = (float)(position.x + Math.cos(angle) * radius);
			float ypos = (float)(position.y + Math.sin(angle) * radius);
			GL11.glVertex2f(xpos, ypos);
			angle+=2.0*Math.PI/CIRCLE_VERTICES_NUMBER;
		}
		GL11.glEnd();
	}

	private void drawPolygon(PolygonShape shape){

		//		int vertexCount = shape.getVertexCount();
		//		GL11.glBegin(GL11.GL_POLYGON);
		//		for (int i = 0; i < vertexCount; i++) {
		//			Vec2 p = shape.getVertex(i);
		//			GL11.glVertex2f(p.x, p.y);
		//		}
		//		GL11.glEnd();
	}

	public static void main(String[] args) {
		new PainterGl();
	}

}
