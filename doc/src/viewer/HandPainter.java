package viewer;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;


public class HandPainter extends Thread{
	
	private boolean stop = false;
	private int userID;
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private ArrayList<Point2D> vertices = new ArrayList<Point2D>();
	
	public HandPainter(int userID, HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints) {
		this.userID = userID;
		this.joints = joints;
	}
	
	public void reset() {
		vertices = new ArrayList<Point2D>();
	}
	
	public void stopPainter() {
		stop = true;
	}
	
	public ArrayList<Point2D> getVertices() {
		return vertices;
	}
	
	public Polygon getShape() {
		Polygon shape = new Polygon();
		

		for (Point2D point : vertices) {
			shape.addPoint((int) point.getX(), (int) point.getY());
			
		}
		return shape;
	}
	
	@Override
	public void run() {
		//Hack, pas propre
		while (!stop) {
			Point3D point3D = joints.get(new Integer(userID)).get(SkeletonJoint.RIGHT_HAND).getPosition();
			vertices.add(new Point2D.Double(point3D.getX(), point3D.getY()));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
