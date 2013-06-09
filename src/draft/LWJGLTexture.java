package draft;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * This class's purpose was to store the several textures of our project.
 * We abandon soon this class for a better way of handling textures.
 * @author jonathan
 *
 */
public class LWJGLTexture {
	
	public static synchronized void displayImage(BufferedImage bufferedImage, int x, int y) throws Exception {
		//		Display.setInitialBackground(0.5f, 0.5f, 0.5f);
		//		Display.create();
		if (!Display.isCreated() || bufferedImage == null) {
			return;
		}
//		System.out.println("not returing...");
		// enable textures since we're going to use these for our sprites
				
		GL11.glPushMatrix();
//		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glColor3f(1,1,1);
		GL11.glBindTexture(GL_TEXTURE_2D, -1);
//		bufferedImage = ImageIO.read(new File("data/lama.png"));
		ByteBuffer textureBuffer = convertImageData(bufferedImage);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// produce a texture from the byte buffer
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bufferedImage.getWidth(),
				bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
				textureBuffer);
		// clear screen
		//		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// translate to the right location and prepare to draw
		
		int h = (int) (bufferedImage.getHeight() * 0.5);
		int w = (int) (bufferedImage.getWidth() * 0.5);
		GL11.glTranslatef(x - w/2,y - h/2, 1.0f);
//		System.out.println("Dimension = " + h + " , " + w);
		// draw a quad textured to match the sprite
		GL11.glBegin(GL_QUADS);
		{
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex3f(-1*w/2, -1*h/2, 0);
			
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(1*w/2, -1*h/2, 0);
			
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex3f(1*w/2, 1*h/2, 0);
			
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex3f(-1*w/2, 1*h/2, 0);
			
			
		}
	
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
		
		GL11.glPopMatrix();

	}


//	public void getTexture(String resourceName) throws IOException {
//		glBindTexture(GL_TEXTURE_2D, 1);
//
//		BufferedImage bufferedImage = loadImage(resourceName);
//		ByteBuffer textureBuffer = convertImageData(bufferedImage);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//
//		// produce a texture from the byte buffer
//		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bufferedImage.getWidth(),
//				bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
//				textureBuffer);
//	}

	/**
	 * Convert the buffered image to a texture
	 */
	@SuppressWarnings("rawtypes")
	private static ByteBuffer convertImageData(BufferedImage bufferedImage) {
		ByteBuffer imageBuffer;
		WritableRaster raster;
		BufferedImage texImage;

		ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace
				.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
				true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

		raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
				bufferedImage.getWidth(), bufferedImage.getHeight(), 4, null);
		texImage = new BufferedImage(glAlphaColorModel, raster, true,
				new Hashtable());

		// copy the source image into the produced image
		Graphics g = texImage.getGraphics();
		g.setColor(new Color(0f, 0f, 0f, 0f));
		g.fillRect(0, 0, 256, 256);
		g.drawImage(bufferedImage, 0, 0, null);

		// build a byte buffer from the temporary image
		// that be used by OpenGL to produce a texture.
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer())
				.getData();

		imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();

		return imageBuffer;
	}

	///**
	// * Load a given resource as a buffered image
	// */
	//private BufferedImage loadImage(String ref) throws IOException {
	//	////		URL url = getClass().getClassLoader().getResource(ref);
	//	////
	//	////		// due to an issue with ImageIO and mixed signed code
	//	////		// we are now using good oldfashioned ImageIcon to load
	//	////		// images and the paint it on top of a new BufferedImage
	//	////		Image img = new ImageIcon(url).getImage();
	//	////		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img
	//	////				.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	//	//		BufferedImage bufferedImage = 
	//	//		
	//
	//	return ImageIO.read(new File("test.jpg"));
	//}
}