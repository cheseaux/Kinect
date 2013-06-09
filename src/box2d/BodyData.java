package box2d;

import java.awt.Color;

/**
 * Cette classe sert de conteneur pour chaque Body.
 * Elle permet de stocker les informations concernant
 * la couleur, la texture et le type de body dont il s'agit
 * @author Jonathan cheseaux et William Trouleau
 *
 */
public class BodyData {
	/** Color of the body */
	private Color color;
	
	/** Index of the texture */
	private int textureIndex;
	
	/** Type of the body (drawn, wall, etc...)*/
	private String type;
	
	/** Specify if the body will be destroyed after the next world step */
	private boolean destroyed;
	
	/**
	 * Builds a new container for the body
	 * @param info, the type of the body
	 * @param color, the color of the body
	 * @param textureIndex, the texture index of the body
	 */
	public BodyData(String info, Color color, int textureIndex) {
		this.color = color;
		this.type = info;
		this.textureIndex = textureIndex;
		
		destroyed = false;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getTextureIndex() {
		return textureIndex;
	}

	public void setTextureIndex(int textureIndex) {
		this.textureIndex = textureIndex;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
	
	@Override
	public String toString() {
		return "[BodyData] : type=" + type + " ; color=" + color + " ; texture="+ textureIndex;
	}
	
	
}
