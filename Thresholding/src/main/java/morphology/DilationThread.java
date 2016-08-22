package morphology;

import java.util.Set;

import datatypes.PixelPos;

public class DilationThread implements Runnable {

	private double[][] inputImage;
	private Set<PixelPos> structuringElement;
	private int width;
	private int height;
	private int x;
	private int y;
	private double outputPixel;
	
	public DilationThread(Set<PixelPos> structuringElement, double[][] inputImage, int width, int height){
		this.inputImage = inputImage;
		this.structuringElement = structuringElement;
		this.width = width;
		this.height = height;
		this.outputPixel = 0;
	}
	
	public double getOutputPixel() {
		return outputPixel;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void run() {
		double returnVal = Double.MIN_VALUE;

		for(PixelPos element: structuringElement){
			if(x+element.getX() >= 0 && x+element.getX() < width){	
				if(y+element.getY() >= 0 && y+element.getY() < height){
					if(returnVal < inputImage[x+element.getX()][y+element.getY()]){
						returnVal = inputImage[x+element.getX()][y+element.getY()];
					}
				}
			}
		}
		outputPixel = returnVal;
	}

}
