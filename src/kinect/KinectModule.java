/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package kinect;

import gestures.UserHandPainter;

import java.awt.Color;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.ImageGenerator;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.SceneMetaData;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;
import org.jbox2d.common.Vec2;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import demos.DemonstrationsCommon;

/**
 * Cette classe est la liaison entre notre programme et l'API de la Kinect.
 * Elle permet de récupérer les images des différentes caméras, ainsi que les coordonnées
 * des squelettes et être notifié lorsqu'un utilisateur est détecté ou plus.
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class KinectModule
{
	/**
	 * Cet observer nous notifie lorsqu'un nouvel utilisateur est détecté par la Kinect
	 * @author Jonathan Cheseaux et William Trouleau
	 *
	 */
	class NewUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args)
		{

			try
			{
				// Si la calibration n'est pas faite, on l'invoque
				if (skeletonCap.needPoseForCalibration())
				{
					poseDetectionCap.startPoseDetection(calibPose, args.getId());
				}
				else
				{
					skeletonCap.requestSkeletonCalibration(args.getId(), true);
				}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * Cet observateur nous notifie lorsque un utilisateur disparait du champ de vision de la Kinect
	 * @author Jonathan Cheseaux et William Trouleau
	 *
	 */
	class LostUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args)
		{
			DemonstrationsCommon.getInstance().removeUserGesture(args.getId());
			handPainters.remove(args.getId());
			joints.remove(args.getId());

//			TextDisplay.println("Lost user " + args.getId());
		}
	}

	/**
	 * Cet observer nous notifie lorsqu'un nouvel utilisateur est détecté par la Kinect
	 * et est complètement calibré (tracking activé)
	 * @author Jonathan Cheseaux et William Trouleau
	 *
	 */
	class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
	{
		@Override
		public void update(IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args)
		{
			System.out.println("Calibration complete: " + args.getStatus());
			try
			{
				if (args.getStatus() == CalibrationProgressStatus.OK)
				{
					System.out.println("starting tracking "  +args.getUser());
					DemonstrationsCommon.getInstance().addUserGesture(args.getUser());
					skeletonCap.startTracking(args.getUser());
					joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
					handPainters.put(new Integer(args.getUser()), new UserHandPainter(args.getUser()));
					getJoints(args.getUser());
				}
				else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT)
				{
					if (skeletonCap.needPoseForCalibration())
					{
						poseDetectionCap.startPoseDetection(calibPose, args.getUser());
					}
					else
					{
						skeletonCap.requestSkeletonCalibration(args.getUser(), true);
					}
				}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**Variable liées à la Kinect */
	private OutArg<ScriptNode> scriptNode;
	private Context context;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private byte[] imgbytes;
	private float histogram[];
	String calibPose = null;
	HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

	/** HashMap associant un utilitaire de dessin à la main pour chaque User */
	private HashMap<Integer, UserHandPainter> handPainters = new HashMap<Integer, UserHandPainter>();

	/** Définit si l'utilisateur doit être détouré de l'arrière-plan */
	private boolean drawBackground = false;

	/** Dimensions de l'image caméra */
	int width, height;

	/** Chemin d'accès au fichier de configuration */
	private final String SAMPLE_XML_FILE = "SamplesConfig.xml";

	/** Instance du module singleton Kinect */
	private static KinectModule instance;

	private KinectModule()
	{

		try {
			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

			//Activation du mode mirroir (image inversée) et initialisation de la caméra de profondeur
			context.setGlobalMirror(true);
			depthGen = DepthGenerator.create(context);
			DepthMetaData depthMD = depthGen.getMetaData();

			// L'histogramme nous permet de stocker les informations de profondeurs antérieures
			histogram = new float[10000];
			
			// Résolution de la camera de profondeur
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();
			imgbytes = new byte[width*height*3];

			// Initialisation du module responsable du tracking des utilisateurs
			userGen = UserGenerator.create(context);
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();

			//Ajout des différents observers
			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());
			skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());
			
			//Joints du squelette pour chaque user
			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();
			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);
			
			context.startGeneratingAll();

		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	//Permet d'ajouter un observer à l'évènement "user perdu"
	public void addEventObserver(IObserver<UserEventArgs> observer) {
		try {
			userGen.getLostUserEvent().addObserver(observer);
		} catch (StatusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized static KinectModule getInstance() {
		if(instance == null) {
			synchronized (KinectModule.class) {
				instance = new KinectModule();
			}
		}
		return instance;
	}

	/**
	 * Code récupéré sur le site de SimpleOpenNI
	 * @param depth
	 */
	private void calcHist(ShortBuffer depth)
	{
		// reset
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		depth.rewind();

		int points = 0;
		while(depth.remaining() > 0)
		{
			short depthVal = depth.get();
			if (depthVal != 0)
			{
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++)
		{
			histogram[i] += histogram[i-1];
		}

		if (points > 0)
		{
			for (int i = 1; i < histogram.length; i++)
			{
				histogram[i] = 1.0f - (histogram[i] / (float)points);
			}
		}
	}

	/**
	 * Met à jour la camera de profondeur
	 */
	public void updateDepth()
	{
		try {
			context.waitAnyUpdateAll();
			context.setGlobalMirror(true);
			DepthMetaData depthMD = depthGen.getMetaData();
			SceneMetaData sceneMD = userGen.getUserPixels(0);

			ShortBuffer scene = sceneMD.getData().createShortBuffer();
			ShortBuffer depth = depthMD.getData().createShortBuffer();
			calcHist(depth);
			depth.rewind();

			for (int i = 0; i < userGen.getUsers().length; i++) {
				int user = userGen.getUsers()[i];
				if (skeletonCap.isSkeletonTracking(userGen.getUsers()[i])) {
					getJoints(userGen.getUsers()[i]);
				}
				if (joints.containsKey(new Integer(user))) {
					SkeletonJointPosition leftHand = joints.get(new Integer(user)).get(SkeletonJoint.LEFT_HAND);
					SkeletonJointPosition rightHand = joints.get(new Integer(user)).get(SkeletonJoint.RIGHT_HAND);
					SkeletonJointPosition head = joints.get(new Integer(user)).get(SkeletonJoint.HEAD);
					if ((leftHand != null && head != null && head.getConfidence() != 0 && leftHand.getConfidence() != 0)
							&& (rightHand != null && rightHand.getConfidence() != 0)){
						Point3D leftPos = leftHand.getPosition();
						Point3D rightPos = rightHand.getPosition();
						Point leftPoint = new Point();
						leftPoint.setLocation(leftPos.getX(), leftPos.getY());
						Point rightPoint = new Point();
						rightPoint.setLocation(rightPos.getX(), rightPos.getY());
						DemonstrationsCommon.getInstance().getUserGesture(user)
							.updateDepth((int) leftPos.getZ(), leftPoint, (int) head.getPosition().getZ(),
								(int) rightPos.getZ(), rightPoint);
					}

				}
			}

			while(depth.remaining() > 0)
			{
				int pos = depth.position();
				short pixel = depth.get();
				short user = scene.get();
				imgbytes[3*pos] = 0;
				imgbytes[3*pos+1] = 0;
				imgbytes[3*pos+2] = 0;                	

				if (drawBackground || pixel != 0)
				{
					int colorID = user % (colors.length-2) + 1;
					if (user == 0)
					{
						colorID = colors.length-1;
					} 
					if (pixel != 0)
					{
						float histValue = histogram[pixel];
						imgbytes[3*pos] = (byte)(histValue*colors[colorID].getRed());
						imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
						imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
					}
				}
			}
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

	Color colors[] = {Color.RED, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.BLUE, Color.WHITE};
	
	/**
	 * Renvoie la position d'un joint spécifique
	 * @param user, l'ID de l'utilisateur
	 * @param joint, le joint à récupérer
	 * @throws StatusException
	 */
	public synchronized void  getJoint(int user, SkeletonJoint joint) throws StatusException
	{
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
		if (pos.getPosition().getZ() != 0)
		{
			joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
		}
		else
		{
			joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
		}
	}

	public boolean isSkeletonReady(int user) {
		return skeletonCap.isSkeletonTracking(user);
	}

	/**
	 * Renvoie la position de tous les membres d'un utilisateur
	 * @param user, l'ID de l'utilisateur
	 * @throws StatusException
	 */
	public void getJoints(int user) throws StatusException
	{
		getJoint(user, SkeletonJoint.HEAD);
		getJoint(user, SkeletonJoint.NECK);

		getJoint(user, SkeletonJoint.LEFT_SHOULDER);
		getJoint(user, SkeletonJoint.LEFT_ELBOW);
		getJoint(user, SkeletonJoint.LEFT_HAND);

		getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
		getJoint(user, SkeletonJoint.RIGHT_ELBOW);
		getJoint(user, SkeletonJoint.RIGHT_HAND);

		getJoint(user, SkeletonJoint.TORSO);

		getJoint(user, SkeletonJoint.LEFT_HIP);
		getJoint(user, SkeletonJoint.LEFT_KNEE);
		getJoint(user, SkeletonJoint.LEFT_FOOT);

		getJoint(user, SkeletonJoint.RIGHT_HIP);
		getJoint(user, SkeletonJoint.RIGHT_KNEE);
		getJoint(user, SkeletonJoint.RIGHT_FOOT);

	}

	private BufferedImage writePixels(ByteBuffer pixels, int width, int height) {
		int[] packedPixels = new int[width * height * 3];

		int bufferInd = 0;
		for (int row = height - 1; row >= 0; row--) {
			for (int col = 0; col < width; col++) {
				int R, G, B;
				R = pixels.get(bufferInd++);
				G = pixels.get(bufferInd++);
				B = pixels.get(bufferInd++);
				int index = (row * width + col) * 3;
				packedPixels[index++] = R;
				packedPixels[index++] = G;
				packedPixels[index] = B;
			}
		}
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster wr = img.getRaster();
		wr.setPixels(0, 0, width, height, packedPixels);
		return img;
	}

	
	/**
	 * renvoie l'image de la caméra de profondeur
	 * @return
	 */
	public synchronized BufferedImage getDepthTexture() {
		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);
		WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 
		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
		return new BufferedImage(colorModel, raster, false, null);
	}


	/**
	 * Méthode de dessin de cercle OPenGL
	 * @param userID l'ID de l'utilisateur
	 * @param joint la position du joint à dessiner
	 */
	private void drawCircle(int userID, SkeletonJoint joint){
		SkeletonJointPosition pos = joints.get(userID).get(joint);
		Vec2 position = new Vec2(pos.getPosition().getX(), Display.getHeight() - pos.getPosition().getY());
		float radius = 5;
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2f(position.x, position.y);
		float angle = 0.0f;
		for( angle = 0; angle < 2.0*Math.PI + 0.0001; ){
			float xpos = (float)(position.x + Math.cos(angle) * radius);
			float ypos = (float)(position.y + Math.sin(angle) * radius);
			GL11.glVertex2f(xpos, ypos);
			angle+=2.0*Math.PI/20;
		}
		GL11.glEnd();

	}

	/**
	 * Dessine des cercles sur chaque articulation du squelette
	 * Utilisé durant le débuggage
	 * @throws StatusException
	 */
	public void drawSkeletons() throws StatusException
	{
		for (Integer user : userGen.getUsers()) {
			if (!skeletonCap.isSkeletonTracking(user)) {
				continue;
			}
			getJoints(user);
			drawCircle(user, SkeletonJoint.HEAD);
			drawCircle(user,SkeletonJoint.NECK);

			drawCircle(user,SkeletonJoint.LEFT_SHOULDER);
			drawCircle(user,SkeletonJoint.LEFT_ELBOW);
			drawCircle(user,SkeletonJoint.LEFT_HAND);

			drawCircle(user,SkeletonJoint.RIGHT_SHOULDER);
			drawCircle(user,SkeletonJoint.RIGHT_ELBOW);
			drawCircle(user,SkeletonJoint.RIGHT_HAND);

			drawCircle(user,SkeletonJoint.TORSO);

			drawCircle(user,SkeletonJoint.LEFT_HIP);
			drawCircle(user,SkeletonJoint.LEFT_KNEE);
			drawCircle(user,SkeletonJoint.LEFT_FOOT);

			drawCircle(user,SkeletonJoint.RIGHT_HIP);
			drawCircle(user,SkeletonJoint.RIGHT_KNEE);
			drawCircle(user,SkeletonJoint.RIGHT_FOOT);
		}

	}

	/**
	 * Renvoie l'image de la caméra RGB
	 * @return une BufferedImage
	 */
	public BufferedImage getRGBImageTexture() {
		ImageGenerator image = null;
		BufferedImage bimg = null;
		try {
			image = org.OpenNI.ImageGenerator.create(context);			
			ByteBuffer bufferImage = image.getMetaData().getData().createByteBuffer();
			bimg = writePixels(bufferImage, 640, 480);
			return bimg;
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retourn un tableau d'entier comprenant les ID des utilisateurs
	 * @return
	 */
	public int[] getUsers() {
		try {
			return userGen.getUsers();
		} catch (StatusException e) {
			e.printStackTrace();
		}
		return null;
	}

	public HashMap<Integer, UserHandPainter> getHandPainters() {
		return handPainters;
	}

	public HashMap<SkeletonJoint, SkeletonJointPosition> getJointsMap(int userID) {
		return joints.get(new Integer(userID));
	}

	public Context getContext() {
		return context;
	}

}

