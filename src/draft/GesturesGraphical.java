package draft;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class was designed for debug purpose
 * It displays the gesture detection on screen
 * @author jonathan
 *
 */
public class GesturesGraphical extends JFrame {
	
	private static final long serialVersionUID = 156944923116091632L;
	
	private Container container;
	
	public JLabel lblRight;
	public JLabel lblLeft;
	
	public GesturesGraphical() {
		this.container = getContentPane();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(new Dimension(600,500));
		buildPanel();
		setVisible(true);
	}

	private void buildPanel() {
		JPanel boxHorizontal = new JPanel();
		boxHorizontal.setLayout(new BoxLayout(boxHorizontal, BoxLayout.X_AXIS));
		
		lblRight = new JLabel("Right Mouse position");
		lblRight.setFont(new Font("Arial", Font.BOLD, 25));
		lblRight.setOpaque(true);
		lblLeft = new JLabel("Left mouse position");
		lblLeft.setFont(new Font("Arial", Font.BOLD, 25));
		lblLeft.setOpaque(true);
	
		
		boxHorizontal.add(lblLeft);
		boxHorizontal.add(Box.createHorizontalGlue());
		boxHorizontal.add(lblRight);
		
		
		container.add(boxHorizontal, BorderLayout.CENTER);
		
	}

}
