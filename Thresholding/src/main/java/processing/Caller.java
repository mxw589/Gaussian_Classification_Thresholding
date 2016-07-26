package processing;

import datatypes.PixelsValues;
import ij.ImagePlus;

public class Caller {
	
	private ImagePlus chosenImg;
	private ImagePlus chosenMask;
	public final int neighbours;
	private PixelsValues[][] readImage;
	public final int IMAGE_WIDTH;
	public final int IMAGE_HEIGHT;
	private ImagePlus resultantImage;
	
	
	public Caller(ImagePlus chosenImg, ImagePlus chosenMask, int neighbours){
		this.chosenImg = chosenImg;
		this.chosenMask = chosenMask;
		this.neighbours = neighbours;
		
		this.IMAGE_WIDTH = chosenImg.getWidth();
		this.IMAGE_HEIGHT = chosenImg.getHeight();
	}

	public ImagePlus getChosenImg() {
		return chosenImg;
	}

	public ImagePlus getChosenMask() {
		return chosenMask;
	}
	
	public PixelsValues[][] getReadImage() {
		return readImage;
	}

	public void setReadImage(PixelsValues[][] readImage) {
		this.readImage = readImage;
	}

	public ImagePlus getResultantImage() {
		return resultantImage;
	}

	public void setResultantImage(ImagePlus resultantImage) {
		this.resultantImage = resultantImage;
	}

	public int getNeighbours() {
		return neighbours;
	}

	public void call(){
		Reader reader = new Reader(this);
		reader.read();
		Classifier classifier = new Classifier(this);
		classifier.calculateMeans();
		
	}
	
}
