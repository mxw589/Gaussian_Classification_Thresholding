package filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import datatypes.PixelPos;

public class MedianFilter {

	public static double[][] medianFilter(Set<PixelPos> structuringElement, double[][] image, int width, int height){
		double[][] returnImg = new double[width][height];
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				returnImg[x][y] = filterPix(structuringElement, image, width, height, x, y);
			}
		}
		
		return returnImg;
	}
	
	public static double filterPix(Set<PixelPos> structuringElement, double[][] image, int width, int height, int x, int y){
		double returnVal;
		
		double strictness = 1.5;
		
		ArrayList<Double> values = new ArrayList<Double>();
		
		for(PixelPos element: structuringElement){
			if(x+element.getX() >= 0 && x+element.getX() < width){
				if(y+element.getY() >= 0 && y+element.getY() < height){
					values.add(image[x+element.getX()][y+element.getY()]);
				}
			}
		}
		
		Collections.sort(values);
		
		if(values.get((values.size() - 1)) < (image[x][y] / strictness)){
			returnVal = values.get((values.size() / 2));
//			System.out.println("Replaced: (" + x + "," + y + ") Was: " + image[x][y] + " Now: " + returnVal);
		} else {
			returnVal = image[x][y];
		}
		
		return returnVal;
	}
	
}
