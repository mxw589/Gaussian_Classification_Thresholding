package processing;

import java.util.HashSet;
import java.util.Set;

import datatypes.PixelPos;
import morphology.Opening;
/**
 * the class performs the task of background removal. It does this by
 * first performing a morphological opening of the image, and then
 * calculating a top-hat transform using that opening
 * @author Mark
 *
 */
public class BackgroundRemoval {

	private Caller caller;
	private Set<PixelPos> structuringElement;
	
	/**
	 * the constructor for the class. this class will be used to
	 * perform the background removal of both the training image
	 * and the testing image, therefore the main purpose of this
	 * constructor is to give access to key variables in the caller
	 * class, and to initiate the construction of the building of 
	 * the set of pixels that will form the structuring element.
	 * @param caller
	 */
	public BackgroundRemoval(Caller caller){
		this.caller = caller;
		buildSE();
	}

	public Caller getCaller() {
		return caller;
	}

	public Set<PixelPos> getStructuringElement() {
		return structuringElement;
	}

	public void setStructuringElement(Set<PixelPos> structuringElement) {
		this.structuringElement = structuringElement;
	}
	
	/**
	 * Builds the structuring element. It gets the radius required from
	 * the caller object where this information is stored
	 */
	private void buildSE(){
		int radius = getCaller().getElementRadius();
		
		Set<PixelPos> structuringElement = new HashSet<PixelPos>();
		
		for(int pixX = -1 * radius; pixX <= radius; pixX++){
			for(int pixY = -1 * radius; pixY <= radius; pixY++){
				if((pixX*pixX) + (pixY * pixY) <= (radius * radius)){
					structuringElement.add(new PixelPos(pixX, pixY));
				}
			}
		}
		
		setStructuringElement(structuringElement);
		
//		System.out.println("SE elements");
//		for(PixelPos ele : structuringElement){
//			System.out.println("" + ele.getX() + "," + ele.getY());
//		}
	}
	
	/**
	 * this method performs the background removal from the image that it
	 * is given. This behaves almost like a static method, but it has access
	 * to the structuring element built by the constructor of this class
	 * @param initialVals the initial image to perform the top-hat
	 * transform on
	 * @param width the width of the initial image
	 * @param height the height of the intial image
	 * @return the image after the top hat transform has been performed on it
	 */
	public double[][] backgroundRemove(double[][] initialVals, int width, int height){
		double[][] background = Opening.open(caller, getStructuringElement(), initialVals, width, height);
		double[][] returnVals = new double[width][height];
				
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				returnVals[x][y] = initialVals[x][y] - background[x][y];
			}
		}
		
		return returnVals;
	}
	
	
}
