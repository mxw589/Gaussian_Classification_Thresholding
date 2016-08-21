package processing;

import java.util.HashSet;
import java.util.Set;

import datatypes.PixelPos;
import morphology.Opening;

public class BackgroundRemoval {

	private Caller caller;
	private Set<PixelPos> structuringElement;
	
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
	
	public double[][] backgroundRemove(double[][] initialVals, int width, int height){
		double[][] background = Opening.open(getStructuringElement(), initialVals, width, height);
		double[][] returnVals = new double[width][height];
				
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				returnVals[x][y] = initialVals[x][y] - background[x][y];
			}
		}
		
		return returnVals;
	}
	
	
}
