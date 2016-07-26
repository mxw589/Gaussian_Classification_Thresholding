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
		Reader.BRIGHTEST_PIXEL = getChosenImg().getMax();
		Reader.DIMMEST_PIXEL = getChosenImg().getMin();	
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
	public void read(){
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
			for(int widthP = 0; widthP < width; widthP++){

				h = linearStretch(getChosenImg().getf(widthP, heightP));
				maskH = getChosenMask().getf(widthP, heightP);

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
		}
		
		getCaller().setReadImage(pixelsValues);
	}
	
	private static double linearStretch(double val){
		return (val - DIMMEST_PIXEL) * (255.0 / (BRIGHTEST_PIXEL - DIMMEST_PIXEL));
	}

}
