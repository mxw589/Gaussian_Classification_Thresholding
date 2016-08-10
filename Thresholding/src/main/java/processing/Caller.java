package processing;

import datatypes.PixelsValues;
import ij.ImagePlus;

public class Caller {
	
	private ImagePlus chosenImg;
	private ImagePlus chosenMask;
	private ImagePlus TBCImg;
	public final int neighbours;
	private PixelsValues[][] readImage;
	public final int IMAGE_WIDTH;
	public final int IMAGE_HEIGHT;
	public final int TBC_IMAGE_WIDTH;
	public final int TBC_IMAGE_HEIGHT;
	private double[][] TBCImage;
	private ImagePlus resultantImage;
	
	
	
	public Caller(ImagePlus chosenImg, ImagePlus chosenMask, ImagePlus TBCImg, int neighbours){
		this.chosenImg = chosenImg;
		this.chosenMask = chosenMask;
		this.TBCImg = TBCImg;
		this.neighbours = neighbours;
		
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
		reader.readClassifier();
		reader.readTBCImage();
		
		Classifier classifier = new Classifier(this);
		classifier.calculateMeans();
		classifier.calculateCovariance();
		
		int step = getNeighbours()/2;
		double[][] predClasses = new double[TBC_IMAGE_WIDTH][TBC_IMAGE_HEIGHT];
		
		for(int heightP = 0 + step; heightP  < TBC_IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < TBC_IMAGE_WIDTH - step; widthP++){
				double[] val = getVal(getTBCImage(), widthP, heightP);
				predClasses[widthP][heightP] = classifier.predictedClass(val);
			}
		}
		
		setTBCImage(predClasses);
		
		ImageBuilder imageBuilder = new ImageBuilder(this, predClasses);
		imageBuilder.buildImage();
		
		getResultantImage().show();
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
	
}
