package datatypes;

/**
 * A data structure for holding x and y co-ordinates of points. This class
 * will be used in a variety of areas throughout the project, including 
 * comprising the Sets of positions that comprise a structuring element, and
 * the positions of pixels within a PixelsValues object
 * @author Mark
 *
 */
public class PixelPos{
	private int x;
	private int y;
	
	/**
	 * Constructor. Given co-ordinates describe the nature of the
	 * pixels position
	 * @param x the relative x co-ordinate of the pixel
	 * @param y the relative y co-orindate of the pixel
	 */
	public PixelPos(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * getter for the relative x position of the pixel
	 * @return the relative x position of the pixel
	 */
	public int getX() {
		return x;
	}

	/**
	 * getter for the relative y position of the pixel
	 * @return the relative y position of the pixel
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * toString method for debugging
	 */
	public String toString(){
		return "" + x + "," + y;
	}

	/**
	 * Equals method that allows for the inclusion of PixelPos objects
	 * in sets without having repeating elements.
	 * @param o
	 * @return true if the pixels are equals, false if not
	 */
	public boolean equals(PixelPos o) {
		boolean checkX = false;
		boolean checkY = false;
		
		if(o.getX() == this.getX()){
			checkX = true;
		}
		
		if(o.getY() == this.getY()){
			checkY = true;
		}
		
		return checkX && checkY;
	}
}
