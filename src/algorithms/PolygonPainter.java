package algorithms;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import algorithms.PolygonSimplificationAlgorithm;
import algorithms.Triangle;
import algorithms.Triangulator;

/**
 * Cette classe est utilisée pour tester les différents algorithmes liés
 * à la manipulation de poylgones
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class PolygonPainter extends JFrame implements KeyListener{

	/**Le panel de dessin */
	private PanelPainter painter;
	
	/**Serial UID */
	private static final long serialVersionUID = 1L;

	/**
	 * Construit une instance de PolygonPainter
	 */
	public PolygonPainter() {
		addKeyListener(this);
		setSize(new Dimension(300,500));
		setVisible(true);
		painter = new PanelPainter();
		add(painter);
	}

	/**
	 * Main de lancement
	 * @param args, pas besoin
	 */
	public static void main(String[] args) {
		new PolygonPainter();
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			painter.simplify();
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			painter.reset();
		} else if (e.getKeyCode() == KeyEvent.VK_0) {
			painter.triangulate();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Cette classe représente le Panel de dessin
	 * @author jonathan
	 *
	 */
	class PanelPainter extends JPanel implements MouseListener, MouseMotionListener{

		private static final long serialVersionUID = 1L;
		private Polygon polygon;
		private Triangle[] triangles = null;
		private PolygonSimplificationAlgorithm algo;
		private boolean flagPressed = false;
		private Polygon finalPolygon = null;

		/**
		 * Construit une nouvelle instance de ce panel
		 */
		public PanelPainter() {
			polygon = new Polygon();
			algo = new PolygonSimplificationAlgorithm(12);
			addMouseListener(this);
			addMouseMotionListener(this);

		}

		//		public void simplify() {
		//			System.out.println("avant simplification : " + polygon.npoints + " vertices");
		//			PolygonSimplificationAlgorithm algo = new PolygonSimplificationAlgorithm(5);
		//			
		//			polygon = algo.simplifyPolygon(polygon);
		//			System.out.println("après simplification : " + polygon.npoints + " vertices");
		//			repaint();
		//		}

		/**
		 * Efface le panel de dessin
		 */
		public void reset() {
			polygon = new Polygon2D();
			repaint();
		}

		/**
		 * invoque l'algorithm de simplification sur le polygon
		 */
		public void simplify() {
			finalPolygon = algo.simplifyPolygon(polygon);
			repaint();
		}

		/**
		 * Triangule le polygon
		 */
		public void triangulate() {
			triangles = Triangulator.triangulatePolygon(finalPolygon);
			if (triangles == null) {
				System.out.println("Triangles was null, trying with CCW order");
				finalPolygon = Triangulator.invertClockOrder(finalPolygon);
				triangles = Triangulator.triangulatePolygon(finalPolygon);

				if (triangles == null) {
					System.err.println("Even in CCW order doesn't work");
				}
			}
			repaint();
		}

		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			super.paint(g);
			if (finalPolygon != null) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(5));
				g2d.drawPolygon(finalPolygon);
				g2d.setColor(Color.BLACK);
				g2d.setStroke(new BasicStroke(1));
			}

			if (triangles != null) {
				for (Triangle t : triangles) {
					g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random())); 
					g.fillPolygon(t.x, t.y, 3);
					System.out.println("dRAWING POLYGON");
				}
			}
			if (triangles == null) {
				for (int i = 0; i < polygon.npoints; i++) {
					g.drawOval(polygon.xpoints[i], polygon.ypoints[i], 3, 3);
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			flagPressed = !flagPressed;

			//			Point p = e.getPoint();
			//			polygon.addPoint(p.x, p.y);
			//			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			//			System.out.println("Mouse move..");
			if (flagPressed) {

				Point p = e.getPoint();
				polygon.addPoint(p.x, p.y);
				repaint();
			}
		}


	}

}
