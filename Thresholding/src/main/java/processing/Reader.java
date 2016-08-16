package processing;

import datatypes.PixelPos;
import datatypes.PixelsValues;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Reader {
	
	private Caller caller;
	private ImageProcessor chosenImg;
	private ImageProcessor chosenMask;
	private ImageProcessor TBCImage;
	
	public Reader(Caller caller){
		this.caller = caller;
		this.chosenImg = caller.getChosenImg().getProcessor();
		this.chosenMask = caller.getChosenMask().getProcessor();
		this.TBCImage = caller.getTBCImg().getProcessor();
	}

	public Caller getCaller() {
		return caller;
	}

	public ImageProcessor getChosenImg() {
		return chosenImg;
	}

	public ImageProcessor getChosenMask() {
		return chosenMask;
	}

	public ImageProcessor getTBCImage() {
		return TBCImage;
	}

	/**
	 * uses the input images in the Caller object in order to create
	 * the ImageLine objects in the Caller object
	 * @param caller
	 */
	public void readClassifier(){
		int width = getCaller().IMAGE_WIDTH;
		int height = getCaller().IMAGE_HEIGHT;

		if(width != getChosenMask().getWidth() || height != getChosenMask().getHeight()){
			throw new IllegalArgumentException("Image and mask don't have the same"
					+ "dimensions");
		}

		PixelsValues[][] pixelsValues = new PixelsValues[width][height];
		PixelPos pixelPos;
		double h;
		double maskH;
		int number = 0;

		for(int heightP = 0; heightP < height; heightP++){
//			System.out.print("[");
			for(int widthP = 0; widthP < width; widthP++){
				h = getChosenImg().getf(widthP, heightP);
				maskH = getChosenMask().getf(widthP, heightP);
				
//				System.out.print(maskH + ",");
				
				pixelPos = new PixelPos(widthP, heightP);
				
				if(maskH >= 0 && maskH < 80){
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "background");
				} else if(maskH >= 88 && maskH < 160){
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "border");
				} else {
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "foreground");
				}
				number++;
			}
//			System.out.println("]");
		}
		
//		System.out.println("linearly scaled:");
//		for(int heightP = 0; heightP < height; heightP++){
//			System.out.print("[");
//			for(int widthP = 0; widthP < width; widthP++){
//				System.out.print(pixelsValues[widthP][heightP].getMaskVal() + ",");
//			}
//			System.out.println("]");
//		}
		
		getCaller().setReadImage(pixelsValues);
		
	}
	
	public void readTBCImage(){
		int width = getCaller().TBC_IMAGE_WIDTH;
		int height = getCaller().TBC_IMAGE_HEIGHT;

		double[][] pixelsValues =  readImage(width, height, getTBCImage());
		
		getCaller().setTBCImage(pixelsValues);
	}
	
	private static double[][] readImage(int width, int height, ImageProcessor image){
		
		double[][] pixelsValues = new double[width][height];
		double h;

		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				h = image.getf(widthP, heightP);
				
				pixelsValues[widthP][heightP] = h;
			}
		}
		
		return pixelsValues;
	}
	
	public static double linearStretch(double val, double dimmest, double brightest){
		return (val - dimmest) * (255.0 / (brightest - dimmest));
	}

}
