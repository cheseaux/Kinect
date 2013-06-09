package demos;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.io.IOException;

import kinect.KinectModule;

import org.OpenNI.Context;
import org.OpenNI.StatusException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

/**
 * Cette démonstration affiche la caméra RGB ainsi que la caméra
 * de profondeur à l'utilisateur, via la Kinect
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class CamerasDemonstration extends Demonstrations{

	/**Le module gérant les données fournies par la Kinect */
	private KinectModule kinect;
	
	/** Les différentes textures de profondeur, rgb */
	private Texture depthMapTex;
	private Texture rgbMapTex;
	
	private int turn = 0;

	/** OpenNI variables */
	private Context context;

	/**
	 * Initialise la démonstration
	 */
	public CamerasDemonstration() {
		super(DemoType.CAMERAS);
		kinect = KinectModule.getInstance();
		
		try {
			// on charge les différentes textures en mémoire
			depthMapTex = BufferedImageUtil.getTexture("rgbImage", kinect.getDepthTexture());
			rgbMapTex = BufferedImageUtil.getTexture("rgbImage", kinect.getRGBImageTexture());
		} catch (IOException e) {
			e.printStackTrace();
		}

		context = kinectModule.getContext();
	
	}

	@Override
	public void setProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	/**
	 * Demande une nouvelle image de profondeur à la Kinect
	 */
	private void UpdateCameraDepth() {
		try {
			depthMapTex = BufferedImageUtil.getTexture("depthImage", kinect.getDepthTexture());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Appel OpenGL pour dessiner la caméra de profondeur sur le Display
	 */
	private void drawCameraDepth() {
		depthMapTex.bind();
		drawCameraTexture();
	}

	/**
	 * Demande une nouvelle image de la caméra RGB de la Kinect
	 */
	private void updateCameraRGB() {
		try {
			rgbMapTex = BufferedImageUtil.getTexture("rgbImage", kinect.getRGBImageTexture());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Dessine l'image de la caméra RGB à l'écran
	 */
	private void drawCameraRGB() {
		rgbMapTex.bind();
		drawCameraTexture();
	}

	
	/**
	 * Appels OpenGL pour afficher une texture
	 */
	private void drawCameraTexture() {
		GL11.glBegin(GL_QUADS);
		{
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(0, 0, 0);

			GL11.glTexCoord2f(0, 0.95f);
			GL11.glVertex3f(0, 1, 0);

			GL11.glTexCoord2f(0.61f, 0.95f);
			GL11.glVertex3f(1, 1, 0);

			GL11.glTexCoord2f(0.61f, 0);
			GL11.glVertex3f(1, 0, 0);
		}

		GL11.glEnd();
	}
	
	@Override
	public void draw() throws Exception {
		try {
			context.waitAnyUpdateAll();
		}
		catch(StatusException e)
		{  System.out.println(e); 
		System.exit(1);
		}
		
		switch (turn) {
		case 0:
			UpdateCameraDepth();
			break;
			
		case 1:
		case 2:
			updateCameraRGB();
			break;
			
		case 3:
			
			break;
			
		default:
			break;
		}
		
		//RGB IMAGE
		GL11.glPushMatrix();
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glTranslated(Display.getWidth() / 2.0f, 0.0, 0.0f);
		GL11.glScalef(Display.getWidth() / 2.0f, Display.getHeight() / 2.0f, 1.0f);
		drawCameraRGB();
		GL11.glPopMatrix();
		
		//Depth image
		GL11.glPushMatrix();
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glTranslated(Display.getWidth() / 2.0f, Display.getHeight(), 0.0f);
		GL11.glScalef(Display.getWidth()/2f, Display.getHeight()/2f,1.0f);
		GL11.glRotatef(180.0f, 0,0,1);
		drawCameraDepth();
		GL11.glPopMatrix();
		
		//Skeleton image
		GL11.glPushMatrix();
		GL11.glColor3f(0.0f, 1.0f, 0.0f);
		GL11.glTranslated(0.0f, Display.getWidth(), 0.0f);
		GL11.glScalef(Display.getWidth() / 2f, Display.getHeight() / 2f,1.0f);
		GL11.glRotatef(180.0f, 0,0,1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, -1);
		drawCameraTexture();
		kinect.drawSkeletons();
		GL11.glPopMatrix();
		
		turn = (turn+1)%4;
	}


	@Override
	public void pollInput(String key) {
		//Not used...
	}
	
}
