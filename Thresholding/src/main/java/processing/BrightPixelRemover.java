package processing;

import java.util.HashSet;
import java.util.Set;

import datatypes.PixelPos;
import filtering.MedianFilter;

public class BrightPixelRemover {
	
	public static double[][] removeBrightPixel(double[][] image, int width, int height){
		Set<PixelPos> structuringElement = buildSE();
		
		double[][] filtered = MedianFilter.medianFilter(structuringElement, image, width, height);
		
		return filtered;
	}
	
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
