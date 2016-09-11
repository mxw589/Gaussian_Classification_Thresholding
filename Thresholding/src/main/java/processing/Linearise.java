package processing;

/**
 * a class that linearly stretches the values of an image so that
 * they are over the range 0-255
 * @author Mark
 *
 */
public class Linearise {
	
	/**
	 * the method that performs the stretching
	 * @param image the image to be linearly stretched
	 * @param width the width of the image to be stretched
	 * @param height the height of the image to be stretched
	 * @return the stretched brightness values
	 */
	public static double[][] linearise(double image[][], int width, int height){
		double[][] linearisedImg = new double[width][height];
		
		double dimmest = -1;
		double brightest = -1;
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(dimmest == -1 || dimmest > image[x][y]){
					dimmest = image[x][y];
				}
				
				if(brightest == -1 || brightest < image[x][y]){
					brightest = image[x][y];
				}
			}
		}
		
//		System.out.println("Dimmest was: " + dimmest);
//		System.out.println("Brightest was: " + brightest);
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
//				System.out.print("(" + x + "," + y + ") Old: " + image[x][y]);
				linearisedImg[x][y] = linearStretch(image[x][y], dimmest, brightest);
//				System.out.println(" New: " + linearisedImg[x][y]);
			}
		}
		
		return linearisedImg;
	}
	
	/**
	 * helper method for the linear stretch method. Performs the
	 * stretching of a single value
	 * @param val the value to be stretched
	 * @param dimmest the dimmest pixel from the original image
	 * @param brightest the bright pixel from the original image
	 * @return the linearly stretched value of the pixel
	 */
	private static double linearStretch(double val, double dimmest, double brightest){
		return (val - dimmest) * (255.0 / (brightest - dimmest));
	}

}
