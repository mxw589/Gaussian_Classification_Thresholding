package processing;

import datatypes.PixelsValues;
import ij.IJ;
import ij.ImagePlus;
/**
 * This is the master class that handles the calling of all of the tasks
 * associated with the process of thresholding
 * @author Mark
 *
 */
public class Caller {
	
	private ImagePlus chosenImg;
	private ImagePlus chosenMask;
	private ImagePlus TBCImg;
	public final int neighbours;
	public final int elementRadius;
	private PixelsValues[][] readImage;
	public final int IMAGE_WIDTH;
	public final int IMAGE_HEIGHT;
	public final int TBC_IMAGE_WIDTH;
	public final int TBC_IMAGE_HEIGHT;
	private double[][] TBCImage;
	private double[][] TBCImageLinear;
	private ImagePlus resultantImage;
	
	/**
	 * The method to which all of the user defined parameters for the
	 * process are passed
	 * @param chosenImg the training image
	 * @param chosenMask the mask for the training image
	 * @param TBCImg the testing image
	 * @param neighbours the height and width of the structuring element
	 * to be used by the classifier
	 * @param elementRadius the radius of the structuring element to be
	 * used in the background removal
	 */
	public Caller(ImagePlus chosenImg, ImagePlus chosenMask, ImagePlus TBCImg, int neighbours, int elementRadius){
		this.chosenImg = chosenImg;
		this.chosenMask = chosenMask;
		this.TBCImg = TBCImg;
		
		this.neighbours = neighbours;
		this.elementRadius = elementRadius;
		
		this.IMAGE_WIDTH = chosenImg.getWidth();
		this.IMAGE_HEIGHT = chosenImg.getHeight();
		
		this.TBC_IMAGE_WIDTH = TBCImg.getWidth();
		this.TBC_IMAGE_HEIGHT = TBCImg.getHeight();
	}

	public ImagePlus getChosenImg() {
		return chosenImg;
	}

	public ImagePlus getChosenMask() {
		return chosenMask;
	}
	
	public ImagePlus getTBCImg() {
		return TBCImg;
	}

	public double[][] getTBCImage() {
		return TBCImage;
	}

	public void setTBCImage(double[][] tBCImage) {
		TBCImage = tBCImage;
	}
	
	public double[][] getTBCImageLinear() {
		return TBCImageLinear;
	}

	public void setTBCImageLinear(double[][] tBCImageLinear) {
		TBCImageLinear = tBCImageLinear;
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

	public int getElementRadius() {
		return elementRadius;
	}

	/**
	 * this is the method that executes the processing of the image.
	 * It handles the calling of all of the components of the process.
	 */
	public void call(){
		Reader reader = new Reader(this);
		reader.readClassifier();
		reader.readTBCImage();
		
		IJ.log("Read images and masks");
		
		//----------REMOVING BRIGHT PIXELS----------//
		double[][] classiImage = extractVals(getReadImage(), IMAGE_WIDTH, IMAGE_HEIGHT);
		
		IJ.log("Removing bright pixels from classifier image");
		double[][] classiImageBriRem = BrightPixelRemover.removeBrightPixel(classiImage, IMAGE_WIDTH, IMAGE_HEIGHT);
		
//		ImageBuilder imageBuilder0 = new ImageBuilder(this, classiImageBriRem, "briRem");
//		imageBuilder0.buildImage();
		
//		getResultantImage().show();
		IJ.log("DONE");
		
		IJ.log("Removing bright pixels from image to be classified");
		double[][] tbcBriRem = BrightPixelRemover.removeBrightPixel(getTBCImage(), TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
//		ImageBuilder imageBuilder1 = new ImageBuilder(this, tbcBriRem, "briRem");
//		imageBuilder1.buildImage();
		
//		getResultantImage().show();
		IJ.log("DONE");
		//----------REMOVED BRIGHT PIXELS----------//
		
		//----------REMOVING BACKGROUNDS----------//
		BackgroundRemoval bgRm = new BackgroundRemoval(this);
		
		IJ.log("Removing background from classifier image");
		double[][] classiBackRem = bgRm.backgroundRemove(classiImageBriRem, IMAGE_WIDTH, IMAGE_HEIGHT);
//		ImageBuilder imageBuilder2 = new ImageBuilder(this, classiBackRem, "bgRem");
//		imageBuilder2.buildImage();
//		
//		getResultantImage().show();
		IJ.log("DONE");
		
		IJ.log("Removing background from image to be classified");
		double[][] tbcBackRem = bgRm.backgroundRemove(tbcBriRem, TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
//		ImageBuilder imageBuilder3 = new ImageBuilder(this, tbcBackRem, "bgRem");
//		imageBuilder3.buildImage();
//		
//		getResultantImage().show();
		IJ.log("DONE");
		//----------REMOVED BACKGROUNDS----------//
		
		//----------LINEARISING----------//
		IJ.log("Linearising classifier Image");
		double[][] classiLinear = Linearise.linearise(classiBackRem, IMAGE_WIDTH, IMAGE_HEIGHT);
//		ImageBuilder imageBuilder4 = new ImageBuilder(this, classiLinear, "linear");
//		imageBuilder4.buildImage();
//		
//		getResultantImage().show();
		IJ.log("DONE");
		
		IJ.log("Linearising background Image");
		double[][] tbcLinear = Linearise.linearise(tbcBackRem, TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
//		ImageBuilder imageBuilder5 = new ImageBuilder(this, tbcLinear, "linear");
//		imageBuilder5.buildImage();
//		
//		getResultantImage().show();
		IJ.log("DONE");
		//----------DONE LINEARISING----------//
		
		//----------SETTING CLEANED VALUES----------//
		for(int y = 0; y < IMAGE_HEIGHT; y++){
			for(int x = 0; x < IMAGE_WIDTH; x++){
				getReadImage()[x][y].setValue(classiLinear[x][y]);
			}
		}
		
		setTBCImage(tbcLinear);
		
		//----------DONE SETTING CLEANED VALUES----------//
		
		//----------ESTABLISHING CLASSIFIER----------//
		IJ.log("Establishing classifier");
		
		Classifier classifier = new Classifier(this);
		classifier.calculateMeans();
		IJ.log("Means done");
		classifier.calculateCovariance();
		IJ.log("Covar done");
		classifier.calcDetInv();
		IJ.log("Inverses and determinants done");
		IJ.log("Done establishing classifier");
		//----------DONE ESTABLISHING CLASSIFIER----------//
		
		//----------PREDICTING CLASSES----------//
		IJ.log("Classifying points");
		int step = getNeighbours()/2;
		double[][] predClasses = new double[TBC_IMAGE_WIDTH][TBC_IMAGE_HEIGHT];
		
		for(int heightP = 0 + step; heightP  < TBC_IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < TBC_IMAGE_WIDTH - step; widthP++){
				double[] val = getVal(getTBCImage(), widthP, heightP);
				predClasses[widthP][heightP] = classifier.predictedClass(val, widthP, heightP);
			}
		}
		IJ.log("Classified points");
		//----------DONE PREDICTING CLASSES----------//
		
		//----------BUILDING IMAGE----------//
		IJ.log("Building image");
		setTBCImage(predClasses);
		
		ImageBuilder imageBuilder = new ImageBuilder(this, predClasses, "threshold");
		imageBuilder.buildImage();
		
		getResultantImage().show();
		IJ.log("Done building image");
		//----------DONE BUILDING IMAGE----------//

	}

	/**
	 * a helper method that extracts the brightness values from an array
	 * of PixelsValues objects
	 * @param pixVals the array of objects from which the brightness values
	 * are required
	 * @param imgWidth the width of the PixelsValues array
	 * @param imgHeight the height of the PixelsValues array
	 * @return the double array that contains the brightness values of the
	 * image
	 */
	private static double[][] extractVals(PixelsValues[][] pixVals, int imgWidth, int imgHeight){
		double[][] values = new double[imgWidth][imgHeight];
		
		for(int y = 0; y < imgHeight; y++){
			for(int x = 0; x < imgWidth; x++){
				values[x][y] = pixVals[x][y].getValue();
			}
		}
		
		return values;
	}
	
	/**
	 * a helper method for the call method. Receives a 2D array of
	 * brightness values from the testing image, and returns them in a 
	 * vector in the order required for classification of that pixel
	 * @param values the 2D array of values 
	 * @param widthP the x co-ordinate of the pixel to be classified
	 * @param heightP the y co-ordinate of the pixel to be classified
	 * @return the vector of values to be used by the classifier
	 */
	private double[] getVal(double[][] values, int widthP, int heightP) {
		double[] retVal = new double[getNeighbours()*getNeighbours()];
		
		int adjust = getNeighbours() / 2;
		
		for(int windowY = 0; windowY < getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getNeighbours(); windowX++){
				retVal[windowY * getNeighbours() + windowX] += values[widthP + windowX - adjust][heightP + windowY - adjust];
			}
		}
		return retVal;
	}
	
}
