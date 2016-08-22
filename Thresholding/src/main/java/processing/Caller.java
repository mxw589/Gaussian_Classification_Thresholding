package processing;

import datatypes.PixelsValues;
import ij.ImagePlus;

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
	private int windowXNumber;
	private int windowYNumber;
	private int windowWidth;
	private int windowHeight;
	
	
	
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
		
		if(neighbours == 3){
			this.windowXNumber = 10;
			this.windowYNumber = 6;
			this.windowWidth = 141;
			this.windowHeight = 175;
		} else if(neighbours == 5){
			this.windowXNumber = 11;
			this.windowYNumber = 7;
			this.windowWidth = 130;
			this.windowHeight = 152;
		}
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
	
	public int getWindowXNumber() {
		return windowXNumber;
	}

	public int getWindowYNumber() {
		return windowYNumber;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public int getElementRadius() {
		return elementRadius;
	}

	public void call(){
		Reader reader = new Reader(this);
		reader.readClassifier();
		reader.readTBCImage();
		
		System.out.println("Read images and masks");
		
		//----------REMOVING BRIGHT PIXELS----------//
		double[][] classiImage = extractVals(getReadImage(), IMAGE_WIDTH, IMAGE_HEIGHT);
		
		System.out.println("Removing bright pixels from classifier image");
		double[][] classiImageBriRem = BrightPixelRemover.removeBrightPixel(classiImage, IMAGE_WIDTH, IMAGE_HEIGHT);
		
		ImageBuilder imageBuilder0 = new ImageBuilder(this, classiImageBriRem, "briRem");
		imageBuilder0.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
		
		System.out.println("Removing bright pixels from image to be classified");
		double[][] tbcBriRem = BrightPixelRemover.removeBrightPixel(getTBCImage(), TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
		ImageBuilder imageBuilder1 = new ImageBuilder(this, tbcBriRem, "briRem");
		imageBuilder1.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
		//----------REMOVED BRIGHT PIXELS----------//
		
		//----------REMOVING BACKGROUNDS----------//
		BackgroundRemoval bgRm = new BackgroundRemoval(this);
		
		System.out.println("Removing background from classifier image");
		double[][] classiBackRem = bgRm.backgroundRemove(classiImageBriRem, IMAGE_WIDTH, IMAGE_HEIGHT);
		ImageBuilder imageBuilder2 = new ImageBuilder(this, classiBackRem, "bgRem");
		imageBuilder2.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
		
		System.out.println("Removing background from image to be classified");
		double[][] tbcBackRem = bgRm.backgroundRemove(tbcBriRem, TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
		ImageBuilder imageBuilder3 = new ImageBuilder(this, tbcBackRem, "bgRem");
		imageBuilder3.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
		//----------REMOVED BACKGROUNDS----------//
		
		//----------LINEARISING----------//
		System.out.println("Linearising classifier Image");
		double[][] classiLinear = Linearise.linearise(classiBackRem, IMAGE_WIDTH, IMAGE_HEIGHT);
		ImageBuilder imageBuilder4 = new ImageBuilder(this, classiLinear, "linear");
		imageBuilder4.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
		
		System.out.println("Linearising background Image");
		double[][] tbcLinear = Linearise.linearise(tbcBackRem, TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
		ImageBuilder imageBuilder5 = new ImageBuilder(this, tbcLinear, "linear");
		imageBuilder5.buildImage();
		
		getResultantImage().show();
		System.out.println("DONE");
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
		System.out.println("Establishing classifier");
		
		Classifier classifier = new Classifier(this);
		classifier.calculateMeans();
		System.out.println("Means done");
		classifier.calculateCovariance();
		System.out.println("Covar done");
		classifier.calcDetInv();
		System.out.println("Inverses and determinants done");
		System.out.println("Done establishing classifier");
		//----------DONE ESTABLISHING CLASSIFIER----------//
		
		//----------PREDICTING CLASSES----------//
		System.out.println("Classifying points");
		int step = getNeighbours()/2;
		double[][] predClasses = new double[TBC_IMAGE_WIDTH][TBC_IMAGE_HEIGHT];
		
		for(int heightP = 0 + step; heightP  < TBC_IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < TBC_IMAGE_WIDTH - step; widthP++){
				double[] val = getVal(getTBCImage(), widthP, heightP);
				predClasses[widthP][heightP] = classifier.predictedClass(val, widthP, heightP);
			}
		}
		System.out.println("Classified points");
		//----------DONE PREDICTING CLASSES----------//
		
		//----------BUILDING IMAGE----------//
		System.out.println("Building image");
		setTBCImage(predClasses);
		
		ImageBuilder imageBuilder = new ImageBuilder(this, predClasses, "threshold");
		imageBuilder.buildImage();
		
		getResultantImage().show();
		System.out.println("Done building image");
		//----------DONE BUILDING IMAGE----------//

	}
	
	public void callValidation(){
		Reader reader = new Reader(this);
		reader.readClassifier();
		
		int step = getNeighbours()/2;
		
		int fgfg = 0;
		int fgbg = 0;
		int bgbg = 0;
		int bgfg = 0;
		
		int completed = 0;
		
		for(int heightP = 0 + step; heightP  < IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < IMAGE_WIDTH - step; widthP++){
				Classifier classifier = new Classifier(this, widthP, heightP);
				classifier.calculateMeans();
				classifier.calculateCovariance();
				classifier.calcDetInv();
				
				double[][] values = extractVals(getReadImage(), IMAGE_WIDTH, IMAGE_HEIGHT);
				
				double[] val = getVal(values, widthP, heightP);
				double predClass = classifier.predictedClass(val, widthP, heightP);
				
				String maskVal = getReadImage()[widthP][heightP].getMaskVal();				
				if(predClass == 1 && maskVal.equals("foreground")){
					fgfg++;
				} else if (predClass == 1 && maskVal.equals("background")){
					fgbg++;
				} else if (predClass == 1 && maskVal.equals("border")){
					fgbg++;
				} else if (predClass == 0 && maskVal.equals("background")){
					bgbg++;
				} else if (predClass == 0 && maskVal.equals("border")){
					bgbg++;
				} else if (predClass == 0 && maskVal.equals("foreground")){
					bgfg++;
				}
				
				completed++;
				if(completed%100 == 0){
					System.out.println(completed);
				}
			}
		}
		
		System.out.println("fgfg:" + fgfg);
		System.out.println("bgbg:" + bgbg);
		System.out.println("fgbg:" + fgbg);
		System.out.println("bgfg:" + bgfg);
		
//		for(int windowY = 0; windowY < getNeighbours(); windowY++){
//			for(int windowX = 0; windowX < getNeighbours(); windowX++){
//				classifier.PXGivenH(classifier.getFgMeans(), classifier.getFgCovariance(), test);
//				classifier.PXGivenH(classifier.getBgMeans(), classifier.getBgCovariance(), test);
//			}
//		}
	}

	private double[][] extractVals(PixelsValues[][] pixVals, int imgWidth, int imgHeight){
		double[][] values = new double[imgWidth][imgHeight];
		
		for(int y = 0; y < imgHeight; y++){
			for(int x = 0; x < imgWidth; x++){
				values[x][y] = pixVals[x][y].getValue();
			}
		}
		
		return values;
	}
	
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
