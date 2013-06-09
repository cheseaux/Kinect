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
package viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SceneMetaData;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;
import org.w3c.dom.css.RGBColor;

import utils.ArrayHelper;

public class UserTracker extends Component
{
	private static final long serialVersionUID = 1L;
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

	private HashMap<Integer, HandPainter> handPainters = new HashMap<Integer, HandPainter>();

	private boolean drawBackground = false;
	private boolean drawPixels = true;
	private boolean drawSkeleton = true;
	private boolean printID = true;
	private boolean printState = true;
	private Shape rectangle = null;
	private BufferedImage bimg;
	private int width, height;
	private final String SAMPLE_XML_FILE = "SamplesConfig.xml";

	private ArrayHelper arrayHelper;

	public UserTracker()
	{

		try {
			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

			depthGen = DepthGenerator.create(context);
			DepthMetaData depthMD = depthGen.getMetaData();

			histogram = new float[10000];
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();
			arrayHelper = new ArrayHelper(width);
			imgbytes = new byte[width*height*3];

			userGen = UserGenerator.create(context);
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();

			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());
			skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());
			poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver());

			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();

			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			context.startGeneratingAll();
		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



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


	void updateDepth()
	{
		try {

			context.waitAnyUpdateAll();

			DepthMetaData depthMD = depthGen.getMetaData();
			SceneMetaData sceneMD = userGen.getUserPixels(0);

			ShortBuffer scene = sceneMD.getData().createShortBuffer();
			ShortBuffer depth = depthMD.getData().createShortBuffer();
			calcHist(depth);
			depth.rewind();

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
					int colorID = user % (colors.length-1);
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


	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};
	public void getJoint(int user, SkeletonJoint joint) throws StatusException
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
	public void getJoints(int user) throws StatusException
	{
		getJoint(user, SkeletonJoint.HEAD);
		getJoint(user, SkeletonJoint.LEFT_HAND);
		getJoint(user, SkeletonJoint.RIGHT_HAND);
	}

	void printSurface(Point3D handCenter, int user) {
		int count = 0;
		int handIndex = arrayHelper.tabToListCoordinates((int) handCenter.getX(), (int) handCenter.getY());
		int handDepth = (int) handCenter.getZ();
		DepthGenerator.
		while (depth.hasRemaining()) {
			int depthVal = depth.get();
			Point positionTab = arrayHelper.listToTabCoordinates(depth.position());
			if (positionTab.distance(handPosition) <= handRadius) {
				if (Math.abs(depthVal - handDepth) < depthTolerance) {
					count++;
//					scene.put(depth.position(), (short) 456);
				}
			}
		}
		System.out.println("Counted " + count + " pixels for the hand");
	}
}


void drawHands(Graphics g, int user) throws StatusException
{
	getJoints(user);
	HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

	if (dict.get(SkeletonJoint.LEFT_HAND).getConfidence() == 0 || dict.get(SkeletonJoint.RIGHT_HAND).getConfidence() == 0)
		return;

	Point3D pos1 = dict.get(SkeletonJoint.LEFT_HAND).getPosition();
	Point3D pos2 = dict.get(SkeletonJoint.RIGHT_HAND).getPosition();

	printSurface(pos1, user);

	g.fillOval((int)pos1.getX(), (int)pos1.getY(), 50, 50);
	g.fillOval((int)pos2.getX(), (int)pos2.getY(), 50, 50);

}

private void drawMenuBox(Graphics g, int user) throws StatusException {
	getJoints(user);
	HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

	Point3D pos1 = dict.get(SkeletonJoint.HEAD).getPosition();
	Point3D handPos = dict.get(SkeletonJoint.LEFT_HAND).getPosition();
	Point2D hand2DPos = new Point2D.Double(handPos.getX(), handPos.getY());

	if (dict.get(SkeletonJoint.HEAD).getConfidence() == 0)
		return;

	rectangle = new Rectangle((int) (pos1.getX() - 150), (int) pos1.getY(), 50, 50);

	Graphics2D g2d = (Graphics2D) g;
	Color prev = g.getColor();
	HandPainter painter = handPainters.get(new Integer(user));

	if (rectangle.contains(hand2DPos)) {
		if (painter == null || !painter.isAlive()) {
			painter = new HandPainter(user, joints);
			handPainters.put(new Integer(user), painter);
			painter.start();
		} else {
			ArrayList<Point2D> vertices = painter.getVertices();
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int i = 0; i < vertices.size() - 1; i++) {
				Point2D point1 = vertices.get(i);
				Point2D point2 = vertices.get(i+1);
				g2d.drawLine((int) point1.getX(), (int) point1.getY(), (int) point2.getX(), (int) point2.getY());
			} 
		}
		g2d.setColor(Color.GREEN);
	} else {
		if (painter != null && painter.isAlive()) {
			g.setColor(Color.PINK);
			g.fillPolygon(painter.getShape());
			painter.stopPainter();
		}
		g2d.setColor(Color.RED);
	}

	g2d.setStroke(new BasicStroke(5));
	g2d.draw(rectangle);
	g2d.setColor(prev);
}

public void paint(Graphics g)
{
	if (drawPixels)
	{
		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);

		WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		bimg = new BufferedImage(colorModel, raster, false, null);

		g.drawImage(bimg, 0, 0, null);
	}
	try
	{
		int[] users = userGen.getUsers();
		for (int i = 0; i < users.length; ++i)
		{
			Color c = colors[users[i]%colors.length];
			c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

			g.setColor(c);
			if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i]))
			{
				drawHands(g, users[i]);
				drawMenuBox(g, users[i]);
			}

			if (printID)
			{
				Point3D com = depthGen.convertRealWorldToProjective(userGen.getUserCoM(users[i]));
				String label = null;
				if (!printState)
				{
					label = new String(""+users[i]);
				}
				else if (skeletonCap.isSkeletonTracking(users[i]))
				{
					// Tracking
					label = new String(users[i] + " - Tracking");
				}
				else if (skeletonCap.isSkeletonCalibrating(users[i]))
				{
					// Calibrating
					label = new String(users[i] + " - Calibrating");
				}
				else
				{
					// Nothing
					label = new String(users[i] + " - Looking for pose (" + calibPose + ")");
				}

				g.drawString(label, (int)com.getX(), (int)com.getY());
			}
		}
	} catch (StatusException e)
	{
		e.printStackTrace();
	}

}

class NewUserObserver implements IObserver<UserEventArgs>
{
	@Override
	public void update(IObservable<UserEventArgs> observable,
			UserEventArgs args)
	{
		System.out.println("New user " + args.getId());
		try
		{
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
class LostUserObserver implements IObserver<UserEventArgs>
{
	@Override
	public void update(IObservable<UserEventArgs> observable,
			UserEventArgs args)
	{
		System.out.println("Lost user " + args.getId());
		joints.remove(args.getId());
	}
}

class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
{
	@Override
	public void update(IObservable<CalibrationProgressEventArgs> observable,
			CalibrationProgressEventArgs args)
	{
		System.out.println("Calibraion complete: " + args.getStatus());
		try
		{
			if (args.getStatus() == CalibrationProgressStatus.OK)
			{
				System.out.println("starting tracking "  +args.getUser());
				skeletonCap.startTracking(args.getUser());
				joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
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

class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs>
{
	@Override
	public void update(IObservable<PoseDetectionEventArgs> observable,
			PoseDetectionEventArgs args)
	{
		System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
		try
		{
			poseDetectionCap.stopPoseDetection(args.getUser());
			skeletonCap.requestSkeletonCalibration(args.getUser(), true);
		} catch (StatusException e)
		{
			e.printStackTrace();
		}
	}
}

}

