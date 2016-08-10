package processing;

import datatypes.PixelPos;
import datatypes.PixelsValues;
import ij.process.ImageProcessor;

public class Reader {
	
	private Caller caller;
	public static double DIMMEST_PIXEL;
	public static double BRIGHTEST_PIXEL;
	private ImageProcessor chosenImg;
	private ImageProcessor chosenMask;
	
	public Reader(Caller caller){
		this.caller = caller;
		this.chosenImg = caller.getChosenImg().getProcessor();
		this.chosenMask = caller.getChosenMask().getProcessor();
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

	/**
	 * uses the input images in the Caller object in order to create
	 * the ImageLine objects in the Caller object
	 * @param caller
	 * @return
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
		double brightest = -1;
		double darkest = -1;

		for(int heightP = 0; heightP < height; heightP++){
//			System.out.print("[");
			for(int widthP = 0; widthP < width; widthP++){

//				System.out.print(getChosenImg().getf(widthP, heightP) + ",");
				
				h = getChosenImg().getf(widthP, heightP);
				maskH = getChosenMask().getf(widthP, heightP);
				if(brightest == -1 || brightest < h){
					brightest = h;
				}
				
				if(darkest == -1 || darkest > h){
					darkest = h;
				}
				
				pixelPos = new PixelPos(widthP, heightP);
				
				if(maskH > 0 && maskH < 80){
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "background");
				} else if(maskH > 81 && maskH < 160){
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "border");
				} else {
					pixelsValues[widthP][heightP] = new PixelsValues(pixelPos, h, number, "foreground");
				}
				number++;
			}
//			System.out.println("]");
		}
		
		DIMMEST_PIXEL = darkest;
//		System.out.println("dim: " + DIMMEST_PIXEL);
		BRIGHTEST_PIXEL = brightest;
//		System.out.println("bright: " + BRIGHTEST_PIXEL);
		
		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				pixelsValues[widthP][heightP].setValue(linearStretch(pixelsValues[widthP][heightP].getValue()));
			}
		}
		
//		System.out.println("linearly scaled:");
//		for(int heightP = 0; heightP < height; heightP++){
//			System.out.print("[");
//			for(int widthP = 0; widthP < width; widthP++){
//				System.out.print(pixelsValues[widthP][heightP].getValue() + ",");
//			}
//			System.out.println("]");
//		}
		
		getCaller().setReadImage(pixelsValues);
		
	}
	
	public void readTBCImage(){
		int width = getCaller().TBC_IMAGE_WIDTH;
		int height = getCaller().TBC_IMAGE_HEIGHT;

		double[][] pixelsValues = new double[width][height];
		double h;
		double brightest = -1;
		double darkest = -1;

		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				h = getChosenImg().getf(widthP, heightP);
				if(brightest == -1 || brightest < h){
					brightest = h;
				}
				
				if(darkest == -1 || darkest > h){
					darkest = h;
				}
				
				pixelsValues[widthP][heightP] = h;
			}
		}
		
		DIMMEST_PIXEL = darkest;
		BRIGHTEST_PIXEL = brightest;
		
		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				pixelsValues[widthP][heightP] = linearStretch(pixelsValues[widthP][heightP]);
			}
		}
		
		getCaller().setTBCImage(pixelsValues);
	}
	
	private static double linearStretch(double val){
		return (val - DIMMEST_PIXEL) * (255.0 / (BRIGHTEST_PIXEL - DIMMEST_PIXEL));
	}

}
