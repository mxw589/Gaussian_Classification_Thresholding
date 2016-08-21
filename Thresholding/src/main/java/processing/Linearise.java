package processing;

public class Linearise {
	
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
	
	private static double linearStretch(double val, double dimmest, double brightest){
		return (val - dimmest) * (255.0 / (brightest - dimmest));
	}

}
