package morphology;

import java.util.Set;

import datatypes.PixelPos;

public class Opening {

	public static double[][] open(Set<PixelPos> structuringElement, double[][] image, int width, int height){
		
		double[][] dilated = new double[width][height];
		double[][] returnVals = new double[width][height];
		
		System.out.print("ero done: ");
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				dilated[x][y] = pixelErode(structuringElement, image, width, height, x, y);
			}
			if((y+1)%(height/10) == 0){
				System.out.print("-");
			}
		}
		System.out.println();
		System.out.print("dil done: ");
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				returnVals[x][y] = pixelDilate(structuringElement, dilated, width, height, x, y);
			}
			if((y+1)%(height/10) == 0){
				System.out.print("-");
			}
		}
		
		return returnVals;
	}
	
	public static double pixelErode(Set<PixelPos> structuringElement, double[][] image, int width, int height, int x, int y){
		double returnVal = Double.MAX_VALUE;
//		System.out.println("(" + x + "," + y + ")");
		
//		int checkNo =0;
//		int fromX = 0;
//		int fromY = 0;
		for(PixelPos element: structuringElement){
//			checkNo++;
//			System.out.println(checkNo + ": Checking (" + (x+element.getX()) + "," + (y+element.getY()) + ")");
			if(x+element.getX() >= 0 && x+element.getX() < width){
				
				if(y+element.getY() >= 0 && y+element.getY() < height){
//					System.out.println("Valid. Current: "+ returnVal + " Checked: " + image[x+element.getX()][y+element.getY()]);
					if(returnVal > image[x+element.getX()][y+element.getY()]){
//						System.out.println("replacing!");
//						fromX = x+element.getX();
//						fromY = y+element.getY();
						returnVal = image[x+element.getX()][y+element.getY()];
					}
				}
			}
		}
//		System.out.println("Done: (" + x + "," + y + ") original: " + image[x][y] + " now: " + returnVal + "from: " + "(" + fromX + "," + fromY + ")");
		return returnVal;
	}

	public static double pixelDilate(Set<PixelPos> structuringElement, double[][] image, int width, int height, int x, int y){
		double returnVal = Double.MIN_VALUE;
//		System.out.println("(" + x + "," + y + ")");
		
//		int checkNo =0;
//		int fromX = 0;
//		int fromY = 0;
		for(PixelPos element: structuringElement){
//			checkNo++;
//			System.out.println(checkNo + ": Checking (" + (x+element.getX()) + "," + (y+element.getY()) + ")");
			if(x+element.getX() >= 0 && x+element.getX() < width){
				
				if(y+element.getY() >= 0 && y+element.getY() < height){
//					System.out.println("Valid. Current: "+ returnVal + " Checked: " + image[x+element.getX()][y+element.getY()]);
					if(returnVal < image[x+element.getX()][y+element.getY()]){
//						System.out.println("replacing!");
//						fromX = x+element.getX();
//						fromY = y+element.getY();
						returnVal = image[x+element.getX()][y+element.getY()];
					}
				}
			}
		}
//		System.out.println("Done: (" + x + "," + y + ") original: " + image[x][y] + " now: " + returnVal + "from: " + "(" + fromX + "," + fromY + ")");
		return returnVal;
	}
}
