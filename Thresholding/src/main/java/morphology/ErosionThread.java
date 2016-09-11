package morphology;

import java.util.Set;

import datatypes.PixelPos;
/**
 * This class is a runnable task that handles the erosion of a single pixel
 * from an image
 * @author Mark
 *
 */
public class ErosionThread implements Runnable{

	private double[][] inputImage;
	private Set<PixelPos> structuringElement;
	private int width;
	private int height;
	private int x;
	private int y;
	private double outputPixel;
	/**
	 * The constructor for the class. The x and y co-ordinates of the
	 * particular pixel that needs to be eroded are not passed on 
	 * construction, they are set manually later
	 * @param structuringElement the set of relative pixel positions that
	 * define the structuring element to be used by the erosion
	 * @param inputImage the image from which a particular pixel will
	 * be eroded
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	public ErosionThread(Set<PixelPos> structuringElement, double[][] inputImage, int width, int height){
		this.inputImage = inputImage;
		this.structuringElement = structuringElement;
		this.width = width;
		this.height = height;
		this.outputPixel = 0;
	}
	
	public double getOutputPixel() {
		return outputPixel;
	}

	/**
	 * setter for the x co-ordinate of the pixel that should be
	 * eroded
	 * @param x the x c-ordinate of the pixel that should be eroded
	 */
	public void setX(int x) {
		this.x = x;
	}
	/**
	 * setter for the y co-ordinate of the pixel that should be
	 * eroded
	 * @param y the y c-ordinate of the pixel that should be eroded
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * the run method for the class that implements the execution of the
	 * erosion on the set x and y co-ordinates
	 */
	public void run() {
		double returnVal = Double.MAX_VALUE;

		for(PixelPos element: structuringElement){
			if(x+element.getX() >= 0 && x+element.getX() < width){	
				if(y+element.getY() >= 0 && y+element.getY() < height){
					if(returnVal > inputImage[x+element.getX()][y+element.getY()]){
						returnVal = inputImage[x+element.getX()][y+element.getY()];
					}
				}
			}
		}
		outputPixel = returnVal;
	}
	
}
