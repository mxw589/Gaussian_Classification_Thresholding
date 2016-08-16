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
		
		System.out.println("Read");
		
		BackgroundRemoval bgRm = new BackgroundRemoval(this);
		
		double[][] bgRmTBC = bgRm.backgroundRemove(getTBCImage(), TBC_IMAGE_WIDTH, TBC_IMAGE_HEIGHT);
		
		setTBCImage(bgRmTBC);
		
		double[][] readImage = extractVals(getReadImage(), IMAGE_WIDTH, IMAGE_HEIGHT);
		
		double[][] bgRmReadImage = bgRm.backgroundRemove(readImage, IMAGE_WIDTH, IMAGE_HEIGHT);
		
		for(int y = 0; y < IMAGE_HEIGHT; y++){
			for(int x = 0; x < IMAGE_WIDTH; x++){
				getReadImage()[x][y].setValue(bgRmReadImage[x][y]);
			}
		}
		
		Classifier classifier = new Classifier(this);
		classifier.calculateMeans();
		System.out.println("Means done");
		classifier.calculateCovariance();
		System.out.println("Covar done");
		classifier.calcDetInv();
		System.out.println("Inverses and determinants done");
		
		int step = getNeighbours()/2;
		double[][] predClasses = new double[TBC_IMAGE_WIDTH][TBC_IMAGE_HEIGHT];
		
		for(int heightP = 0 + step; heightP  < TBC_IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < TBC_IMAGE_WIDTH - step; widthP++){
				double[] val = getVal(getTBCImage(), widthP, heightP);
				predClasses[widthP][heightP] = classifier.predictedClass(val);
			}
		}
		System.out.println("Classified points");
		setTBCImage(predClasses);
		
		ImageBuilder imageBuilder = new ImageBuilder(this, predClasses);
		imageBuilder.buildImage();
		
		getResultantImage().show();
//		double[][] tbcLinear = new double[TBC_IMAGE_WIDTH][TBC_IMAGE_HEIGHT];
//		
//		setTBCImageLinear(tbcLinear);
//		
//		double[][] currWindow = new double[getWindowWidth()][getWindowHeight()];
//		
//		for(int xWindow = 0; xWindow < getWindowXNumber(); xWindow++){
//			for(int yWindow = 0; yWindow < getWindowYNumber(); yWindow++){
//				int windowXStart = windowStartPix(getWindowWidth(), xWindow);
//				int windowXEnd = windowEndPix(getWindowWidth(), xWindow);
//				
//				int windowYStart = windowStartPix(getWindowHeight(), yWindow);
//				int windowYEnd = windowEndPix(getWindowHeight(), yWindow);
//				
//				currWindow = windowVals(windowXStart,windowXEnd,windowYStart,windowYEnd);
//				
//				currWindow = linearStretchedWindow(currWindow, getWindowWidth(), getWindowHeight());
//				
//				updateTBC(getTBCImageLinear(), currWindow, windowXStart, windowXEnd, windowYStart, windowYEnd);
//			}
//		}
//		
//		System.out.println("Stretched image");
		

	}

	private int windowStartPix(int length, int window){
		return (length - (getNeighbours() - 1)) * window;
	}
	
	private int windowEndPix(int length, int window){
		return ((length - (getNeighbours() - 1)) * (window + 1)) + (getNeighbours() - 1);
	}
	
	private double[][] windowVals(int xStart, int xEnd, int yStart, int yEnd){
		double[][] currWindow = new double[getWindowWidth()][getWindowHeight()];
		int currWinX = 0;
		int currWinY = 0;

		for(int y = yStart; y<yEnd; y++){
			for(int x = xStart; x < xEnd; x++){
				currWindow[currWinX][currWinY] = getTBCImage()[x][y];
				currWinX++;
			}
			currWinX = 0;
			currWinY++;
		}
		
		return currWindow;
	}
	
	private void updateTBC(double[][] tbc, double[][] currWindow, int xStart, int xEnd, int yStart, int yEnd){
		int step = (getNeighbours() - 1)/2;
		
		int currWinX = step;
		int currWinY = step;
		
		
		for(int y = yStart + step; y < yEnd - step; y++){
			for(int x = xStart + step; x < xEnd - step; x++){
				tbc[x][y] = currWindow[currWinX][currWinY];
				currWinX++;
			}
			currWinX = step;
			currWinY++;
		}
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
				double predClass = classifier.predictedClass(val);
				
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
	
	private static double[][] linearStretchedWindow(double[][] windowVal, int width, int height){
		double[][] returnValues = new double[width][height];
		double brightest = -1;
		double darkest = -1;
		double h;

		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				h = windowVal[widthP][heightP];
				if(brightest == -1 || brightest < h){
					brightest = h;
				}
				
				if(darkest == -1 || darkest > h){
					darkest = h;
				}
				
				returnValues[widthP][heightP] = h;
			}
		}
		
		for(int heightP = 0; heightP < height; heightP++){
			for(int widthP = 0; widthP < width; widthP++){
				returnValues[widthP][heightP] = Reader.linearStretch(returnValues[widthP][heightP], darkest, brightest);
			}
		}
		
		return returnValues;
	}
}
