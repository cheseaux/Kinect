package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Cette classe permet d'afficher un écran de chargement au lancement de l'application
 * Elle est masquée si ProjectAnimation est lancée en mode FullScreen
 * @author Jonathan Cheseaux et William Trouleau
 *
 */
public class Splash extends JFrame {
	
	/** Seria UID */
	private static final long serialVersionUID = 3142589582007791052L;
	
	/** Label de progression */
	private JLabel lblProgress;
	
	/** Progress bar */
	private JProgressBar progressBar;
	
	/** Image d'arrière plan */
	private BufferedImage backGround;
	
	/** Chemin d'accès à l'arrière-plan */
	private static String splashImagePath = "data/splash.png";

	/**
	 * Lance la fenêtre graphique d'attende de chargement
	 */
	public Splash() {
		setAlwaysOnTop(true);
		loadImage();
		setUndecorated(true);
		setSize(new Dimension(backGround.getWidth(),backGround.getHeight()));
		setLocationRelativeTo(null);
		setLayout(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		buildSplashScreen();
		setVisible(true);
	}
	
	private void loadImage() {
		try {
			backGround = ImageIO.read(new File(splashImagePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void buildSplashScreen() {
		lblProgress = new JLabel("Initialisation de la Kinect ...");
		lblProgress.setOpaque(true);
		lblProgress.setBackground(Color.WHITE);
		lblProgress.setHorizontalAlignment(JLabel.CENTER);
		getContentPane().add(lblProgress);
		lblProgress.setBounds(0, backGround.getHeight() - 30, backGround.getWidth(), 20);
		
		progressBar = new JProgressBar();
		progressBar.setBorderPainted(false);
		progressBar.setBackground(Color.BLACK);
		progressBar.setOpaque(true);
		progressBar.setValue(0);
		progressBar.setMaximum(12);
		getContentPane().add(progressBar);
		progressBar.setBounds(20, backGround.getHeight() - 45, backGround.getWidth() - 40, 15);

	}
	
	/**
	 * Set the progression of the loading
	 * @param str, the string to display
	 * @param value, the value of the progression
	 */
	public void setProgress(String str, int value) {
		lblProgress.setText(str);
		progressBar.setValue(value);
		progressBar.repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(backGround, 0, 0, backGround.getWidth(), backGround.getHeight(), null);
		lblProgress.repaint();
		progressBar.repaint();
	}
	
	public static void main(String[] args) {
		new Splash();
	}

}
