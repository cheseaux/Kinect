package demos;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import gestures.KinectMouseListener;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import kinect.KinectModule;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

/**
 * Démonstration affichant la caméra depth et RGB de la Kinect
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class KinectDemonstration extends Demonstrations implements KinectMouseListener{

	/** Texture ID de l'image de profondeur */
	public static final int DEPTH_TEXTURE = 1;
	
	/** Texture ID de l'image RGB */
	public static final int RGB_TEXTURE = 3;
	private int textureType;
	
	/** Module de kinect */
	private KinectModule kinect;
	private Texture depthMapTex;
	private BufferedImage kinectDepthImage;
	
	private int frame = 0;

	/**
	 * Construit une nouvelle démonstration Kinect
	 * permettant l'affichage de la caméra RGB ainsi que la 
	 * caméra depth de la Kinect
	 * @param texture
	 */
	public KinectDemonstration(int texture) {
		super(DemoType.KINECT);
		setSkeletonVisible(false);
		
		if (texture == 1 || texture == 2 || texture == 3) {
			textureType = texture;
		} else {
			System.err.println("Wrong texture type in KinectDemonstration");
		}
		
		kinect = KinectModule.getInstance();
	}
	
	/**
	 * Redemande une nouvelle image de la caméera de profondeur
	 */
	public void refreshDepthMapTexture() {
		if (textureType == DEPTH_TEXTURE) {
			kinectDepthImage = kinect.getDepthTexture();
			
			// Flip the image vertically
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -kinectDepthImage.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			kinectDepthImage = op.filter(kinectDepthImage, null);
			
		} else if (textureType == RGB_TEXTURE) {
			kinectDepthImage = kinect.getRGBImageTexture();
			
		}
		
		try {
			depthMapTex = BufferedImageUtil.getTexture("kinectDepth", kinectDepthImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Dessine l'image de la caméra de profondeur
	 */
	public void drawKinectDepth() {
		try {
			refreshDepthMapTexture();
			depthMapTex.bind();

			GL11.glPushMatrix();
			GL11.glScalef(Display.getWidth(), Display.getHeight(),1.0f);
			float scaleY = 480.0f/512.0f;
			float scaleX = 640.0f/1024.0f;
			GL11.glBegin(GL_QUADS);
			{
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex3f(0, 0, 0);

				GL11.glTexCoord2f(0, scaleY);
				GL11.glVertex3f(0, 1, 0);

				GL11.glTexCoord2f(scaleX, scaleY);
				GL11.glVertex3f(1, 1, 0);

				GL11.glTexCoord2f(scaleX, 0);
				GL11.glVertex3f(1, 0, 0);

			}

			GL11.glEnd();
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		} catch (Exception e) {
			//Severe, don't know how to fix if kinect crash
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void draw() {
		// Retrieve Kinect image and display it
//		TextDisplay.println("Draw Kinect Demonstration!");
		
		if (frame%2 == 0) {
			kinect.updateDepth();
		}
		frame = (frame+1)%2;
		setProjection();
		drawKinectDepth();
	}

}