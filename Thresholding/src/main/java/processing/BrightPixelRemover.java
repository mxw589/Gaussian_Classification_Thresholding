package processing;

import java.util.HashSet;
import java.util.Set;

import datatypes.PixelPos;
import filtering.MedianFilter;
/**
 * this class contains the set of static methods that implement the custom
 * median filtering of the image
 * @author Mark
 *
 */
public class BrightPixelRemover {
	
	/**
	 * This is the static method that constructs the structuring element
	 * to be used in the median filter, and calls the relevant static method
	 * from the median filter class on the image
	 * @param image the image to which the custom median filter should be
	 * applied
	 * @param width the width of the image
	 * @param height the height of the image
	 * @return the result of the custom median filter being applied to the image
	 */
	public static double[][] removeBrightPixel(double[][] image, int width, int height){
		Set<PixelPos> structuringElement = buildSE();
		
		double[][] filtered = MedianFilter.medianFilter(structuringElement, image, width, height);
		
		return filtered;
	}
	
	/**
	 * the helped method that builds the structuring element for the median filter
	 * @return the set of relative pixel positions that describe the structure of
	 * the structuring element to be used by the median filter
	 */
	private static Set<PixelPos> buildSE(){
		int radius = 3;
		
		Set<PixelPos> structuringElement = new HashSet<PixelPos>();
		
		for(int pixX = (-1 * ((radius-1)/2)); pixX <= ((radius-1)/2); pixX++){
			for(int pixY = (-1 * ((radius-1)/2)); pixY <= ((radius-1)/2); pixY++){
				if(pixX != 0 || pixY != 0){
					structuringElement.add(new PixelPos(pixX, pixY));
				}
			}
		}
		
//		System.out.println("SE elements, filtering");
//		for(PixelPos ele : structuringElement){
//			System.out.println("" + ele.getX() + "," + ele.getY());
//		}
		
		return structuringElement;
	}
}
