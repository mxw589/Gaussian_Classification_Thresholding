package processing;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import datatypes.PixelPos;
import datatypes.PixelsValues;
/**
 * this class handles the classification of pixels. It finds the
 * means and covariance matrix of the foreground and background 
 * in the training image as training for the classifier. It also
 * contains the methods that apply the classifier to new images
 * 
 * @author Mark
 *
 */
public class Classifier {

	private Caller caller;
	private double[][] fgMeans;
	private double[][] bgMeans;
	private double[][] fgCovariance;
	private double[][] bgCovariance;
	private RealMatrix fgMeanCVec;
	private RealMatrix fgCovar;
	private RealMatrix fgCovarInv;
	private double fgCovarDet;
	private RealMatrix bgMeanCVec;
	private RealMatrix bgCovar;
	private RealMatrix bgCovarInv;
	private double bgCovarDet;
	double bgCount;
	double fgCount;
	double borderCount;

	/**
	 * the constructor for the class, all of the required variables
	 * are held in the caller class (the training and testing image)
	 * @param caller
	 */
	public Classifier(Caller caller){
		this.caller = caller;
		this.fgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
		this.bgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
	}

	public double[][] getFgMeans() {
		return fgMeans;
	}

	public double[][] getBgMeans() {
		return bgMeans;
	}

	public double[][] getFgCovariance() {
		return fgCovariance;
	}

	public void setFgCovariance(double[][] fgCovariance) {
		this.fgCovariance = fgCovariance;
	}

	public double[][] getBgCovariance() {
		return bgCovariance;
	}

	public void setBgCovariance(double[][] bgCovariance) {
		this.bgCovariance = bgCovariance;
	}

	public Caller getCaller() {
		return caller;
	}

	public RealMatrix getFgMeanCVec() {
		return fgMeanCVec;
	}

	public void setFgMeanCVec(RealMatrix fgMeanCVec) {
		this.fgMeanCVec = fgMeanCVec;
	}

	public RealMatrix getFgCovarInv() {
		return fgCovarInv;
	}

	public void setFgCovarInv(RealMatrix fgCovarInv) {
		this.fgCovarInv = fgCovarInv;
	}

	public double getFgCovarDet() {
		return fgCovarDet;
	}

	public void setFgCovarDet(double fgCovarDet) {
		this.fgCovarDet = fgCovarDet;
	}

	public RealMatrix getBgMeanCVec() {
		return bgMeanCVec;
	}

	public void setBgMeanCVec(RealMatrix bgMeanCVec) {
		this.bgMeanCVec = bgMeanCVec;
	}

	public RealMatrix getBgCovarInv() {
		return bgCovarInv;
	}

	public void setBgCovarInv(RealMatrix bgCovarInv) {
		this.bgCovarInv = bgCovarInv;
	}

	public double getBgCovarDet() {
		return bgCovarDet;
	}

	public void setBgCovarDet(double bgCovarDet) {
		this.bgCovarDet = bgCovarDet;
	}

	public RealMatrix getFgCovar() {
		return fgCovar;
	}

	public void setFgCovar(RealMatrix fgCovar) {
		this.fgCovar = fgCovar;
	}

	public RealMatrix getBgCovar() {
		return bgCovar;
	}

	public void setBgCovar(RealMatrix bgCovar) {
		this.bgCovar = bgCovar;
	}

	/**
	 * this method calculates the means of the foreground and background
	 * classes as part of training the classifier
	 */
	public void calculateMeans(){
		PixelsValues[][] readImage = getCaller().getReadImage();

		double[][] bgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];
		double[][] fgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];

		int step = getCaller().getNeighbours()/2;

//		System.out.println("(0,0):");
		for(int heightP = 0 + step; heightP  < getCaller().IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < getCaller().IMAGE_WIDTH - step; widthP++){
				if(readImage[widthP][heightP].getMaskVal().equals("foreground")){
					fgCount++;
					totalWindow(fgTotal, widthP, heightP);
				} else if(readImage[widthP][heightP].getMaskVal().equals("background") ||
						readImage[widthP][heightP].getMaskVal().equals("border")){
					bgCount++;
					totalWindow(bgTotal, widthP, heightP);
				}
			}
		}
//		System.out.println("fg totals: ");
//		
//		for(int y = 0; y < getCaller().getNeighbours(); y++){
//			System.out.print("[");
//			for(int x = 0; x < getCaller().getNeighbours(); x++){
//				System.out.print(fgTotal[x][y] + ",");
//			}
//			System.out.println("]");
//		}
		
//		System.out.println("fg means: ");
		for(int y = 0; y < getCaller().getNeighbours(); y++){
//			System.out.print("[");
			for(int x = 0; x < getCaller().getNeighbours(); x++){
//				System.out.print(fgTotal[x][y]/ fgCount + ",");
				getFgMeans()[x][y] = fgTotal[x][y] / fgCount;
				getBgMeans()[x][y] = bgTotal[x][y] / bgCount;
			}
//			System.out.println("]");
		}
	}

	/**
	 * method that calculates the convariance matrix of the elements
	 * of the structuring element as part of training the classifier
	 */
	public void calculateCovariance(){

		double[][] fgeXeY = calcEXEY(getFgMeans());
		double[][] fgeXY = calcEXY("foreground");

		double[][] bgeXeY = calcEXEY(getBgMeans());
		double[][] bgeXY = calcEXY("background");

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		
		double[][] fgCov = new double[nSq][nSq];
		double[][] bgCov = new double[nSq][nSq];
	

		for(int windowY = 0; windowY < nSq; windowY++){
			for(int windowX = 0; windowX < nSq; windowX++){
				fgCov[windowX][windowY] = fgeXY[windowX][windowY] - fgeXeY[windowX][windowY];
				bgCov[windowX][windowY] = bgeXY[windowX][windowY] - bgeXeY[windowX][windowY];
			}
		}
		
//		//Debugging
//		for(int windowY = 0; windowY < nSq; windowY++){
//			for(int windowX = 0; windowX < nSq; windowX++){
//				System.out.print(fgCov[windowX][windowY] + ",");
//			}
//			System.out.println();
//		}
		
		setFgCovariance(fgCov);
		setBgCovariance(bgCov);
	}

	/**
	 * helper method for the calculation of the covariance matrix.
	 * the covariance formula used is E[XY] - E[X]E[Y] and this
	 * method calculates the E[X]E[Y] aspect
	 * @param means the set of means to use in the calculation, should
	 * either be the foreground or background means
	 * @return a matrix that contains the E[X]E[Y] values
	 */
	public double[][] calcEXEY(double[][] means){
//		System.out.println("EXEY:");
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();

		double[][] eXeY = new double[nSq][nSq];

		for(int windowY = 0; windowY < nSq; windowY++){
			int meanX = windowY % getCaller().getNeighbours();
			int meanY = windowY / getCaller().getNeighbours();
//			System.out.print("[");
			double rowMean = means[meanX][meanY];

			for(int windowX = 0; windowX < nSq; windowX++){
				int meanXX = windowX % getCaller().getNeighbours();
				int meanXY = windowX / getCaller().getNeighbours();

				eXeY[windowX][windowY] = rowMean * means[meanXX][meanXY];
//				System.out.print(eXeY[windowX][windowY] + ",");
			}
//			System.out.println("]");
		}

		return eXeY;
	}

	/**
	 * helper method for the calculation of the covariance matrix.
	 * the covariance formula used is E[XY] - E[X]E[Y] and this
	 * method calculates the E[XY] aspect
	 * @param layer a string that indicates if the foreground or
	 * background E[XY] is being calculated. It should be either
	 * 'foreground' or 'background'
	 * @return a matrix of the E[XY] values
	 */
	public double[][] calcEXY(String layer){
		PixelsValues[][] readImage = getCaller().getReadImage();

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		double[][] eXY = new double[nSq][nSq];
		int setCounter = 0;

		int step = getCaller().getNeighbours()/2;
		

		for(int heightP = 0 + step; heightP  < getCaller().IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < getCaller().IMAGE_WIDTH - step; widthP++){
				if(layer.equals("foreground")){
					if(readImage[widthP][heightP].getMaskVal().equals("foreground")){
						alterCoVar(eXY, widthP, heightP);
						setCounter++;
					}
				} else if(layer.equals("background")){
					if(readImage[widthP][heightP].getMaskVal().equals("background") ||
							readImage[widthP][heightP].getMaskVal().equals("border")){
						alterCoVar(eXY, widthP, heightP);
						setCounter++;
					}
				}
			}
		}
		
//		System.out.println("XY");
//		for(int windowY = 0; windowY < nSq; windowY++){
//			System.out.print("[");
//			for(int windowX = 0; windowX < nSq; windowX++){
//				System.out.print(eXY[windowX][windowY] + ",");
//			}
//			System.out.println("]");
//		}
		
//		System.out.println("EXY");
		for(int windowY = 0; windowY < nSq; windowY++){
//			System.out.print("[");
			for(int windowX = 0; windowX < nSq; windowX++){
				eXY[windowX][windowY] = eXY[windowX][windowY] / setCounter;
//				System.out.print(eXY[windowX][windowY] + ",");
			}
//			System.out.println("]");
		}
		
		return eXY;
	}
	
	/**
	 * a helper method for the calcEXY method
	 * increases the values of the mid-calculation covariance matrix to
	 * include the values from a given pixel position
	 * @param covar the current covariance values
	 * @param x the x co-ordinate of the pixel to be included
	 * @param y the y co-ordinate of the pixel to be included
	 */
	public void alterCoVar(double[][] covar, int x, int y){
		PixelsValues[][] readImage = getCaller().getReadImage();

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		
		int adjust = getCaller().getNeighbours() / 2;
		
		for(int windowY = 0; windowY < nSq; windowY++){
			int meanX = windowY % getCaller().getNeighbours();
			int meanY = windowY / getCaller().getNeighbours();

			double xVal = readImage[x + meanX - adjust][y + meanY - adjust].getValue();

			for(int windowX = 0; windowX < nSq; windowX++){
				int meanXX = windowX % getCaller().getNeighbours();
				int meanXY = windowX / getCaller().getNeighbours();

				covar[windowX][windowY] += xVal * readImage[x + meanXX - adjust][y + meanXY - adjust].getValue();
			}
		}
	}

	/**
	 * a helper method for the calculateMeans method
	 * increases the values of the mid-calculation mean matrix to
	 * include the values from a given pixel position
	 * @param covar the current mean values
	 * @param totals
	 * @param x
	 * @param y
	 */
	public void totalWindow(double[][] totals, int x, int y){
		PixelsValues[][] readImage = getCaller().getReadImage();
		
		int adjust = getCaller().getNeighbours() / 2;
		
		for(int windowY = 0; windowY < getCaller().getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				totals[windowX][windowY] += readImage[x + windowX - adjust][y + windowY - adjust].getValue();
				
//				//Debugging
//				if(windowY ==0 && windowX == 0 && test.equals("fg:")){
//					System.out.print(readImage[x + windowX - adjust][y + windowY - adjust].getValue());
//					System.out.print(",");
//				}
//				if(windowY ==0 && windowX == 1){
//					System.out.println("(" + (x + windowX - adjust) + "," +(y + windowY - adjust)+ ")"+"TM:" + readImage[x + windowX - adjust][y + windowY - adjust].getValue());
//				}
			}
		}
	}
	
	/**
	 * after the covariance matrix has been calculated, this method
	 * calculates the determinant and inverse of the resultant
	 * matrix, and stores them for use in the classification of new points
	 */
	public void calcDetInv(){
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		RealMatrix covarMatrix = new Array2DRowRealMatrix(getFgCovariance());
		setFgCovar(covarMatrix);
//		System.out.println(covarMatrix);
		LUDecomposition ludecomp = new LUDecomposition(covarMatrix);
//		System.out.println(covarMatrix.getColumnDimension() + ", " + covarMatrix.getRowDimension());
//		System.out.println(covarMatrix);
		double covarDet = ludecomp.getDeterminant();
		setFgCovarDet(covarDet);
//		System.out.println(covarDet);
		RealMatrix covarInv = ludecomp.getSolver().getInverse();
		setFgCovarInv(covarInv);
//		System.out.println(covarInv);
		
		double[] meanRow = new double[nSq];
		for(int windowY = 0; windowY < getCaller().getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				meanRow[windowY * getCaller().getNeighbours() + windowX] = getFgMeans()[windowX][windowY];
			}
		}
		RealMatrix meanCVec = new Array2DRowRealMatrix(meanRow);
		setFgMeanCVec(meanCVec);
		
		covarMatrix = new Array2DRowRealMatrix(getBgCovariance());
		setBgCovar(covarMatrix);
		ludecomp = new LUDecomposition(covarMatrix);
//		System.out.println(covarMatrix.getColumnDimension() + ", " + covarMatrix.getRowDimension());
//		System.out.println(covarMatrix);
		covarDet = ludecomp.getDeterminant();
		setBgCovarDet(covarDet);
//		System.out.println("BGcovarDet:" +covarDet);
		covarInv = ludecomp.getSolver().getInverse();
		setBgCovarInv(covarInv);
//		System.out.println(covarInv);
		
		meanRow = new double[nSq];
		for(int windowY = 0; windowY < getCaller().getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				meanRow[windowY * getCaller().getNeighbours() + windowX] = getBgMeans()[windowX][windowY];
			}
		}
		meanCVec = new Array2DRowRealMatrix(meanRow);
		setBgMeanCVec(meanCVec);
	}
	
	/**
	 * the method that executes the Gaussian classification formula
	 * @param meanCVec the mean matrix as a column vector
	 * @param covarInv the inverse of the convariance matrix
	 * @param covar the convariance matrix
	 * @param covarDet the determinant of the convariance matrix
	 * @param val the column vector of the values that represent the pixel
	 * to be classified
	 * @param x the x co-ordinate of the pixel that is being classified
	 * @param y the y co-ordinate of the pixle that is being classified
	 * @return the probability of the pixel being in the given class
	 */
	public double PXGivenH(RealMatrix meanCVec, RealMatrix covarInv, RealMatrix covar, double covarDet, double[] val, int x, int y){
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();

		
		RealMatrix valCVec = new Array2DRowRealMatrix(val);

		
		RealMatrix valMinusMean = valCVec.subtract(meanCVec);

//		if(x > 540 && x < 590){
//			if(y > 160 && y < 200){
//				System.out.print("(" + x + "," + y + ") ");
//				System.out.println(" valCVec: "+valCVec);
//				System.out.println(" meanCVec: "+meanCVec);
//				System.out.println(" valMinusMean: "+valMinusMean);
//				System.out.println(" covar: "+ covar);
//				System.out.println(" covarInv: "+covarInv);
//				System.out.println(" covarDet: "+covarDet);
//				System.out.println(" first part: " + (1/(Math.sqrt(Math.pow(2*Math.PI, nSq)*covarDet))));
//				System.out.println(" second part: " + (((valMinusMean.transpose()).multiply(covarInv)).multiply(valMinusMean).getEntry(0, 0)));
//			}
//		}
		return (1/(Math.sqrt(Math.pow(2*Math.PI, nSq)*covarDet))) * Math.exp(-0.5 * ((valMinusMean.transpose()).multiply(covarInv)).multiply(valMinusMean).getEntry(0, 0));
	}
	
	/**
	 * calculates the predicted class of a pixel from its structuring element
	 * values
	 * @param val the pixels structuring element values
	 * @param x the x co-ordinate of the pixel
	 * @param y the y co-ordinate of the pixel
	 * @return the predicted class of the pixel
	 */
	public double predictedClass(double[] val, int x, int y){
		double pClass;
//		if(x > 540 && x < 590){
//			if(y > 160 && y < 200){
//		System.out.println(" fg: ");
//			}
//		}
		double pForeground = PXGivenH(getFgMeanCVec(), getFgCovarInv(), getFgCovar(), getFgCovarDet(), val, x, y);
//		if(x > 540 && x < 590){
//			if(y > 160 && y < 200){
//		System.out.println(" bg: ");
//			}
//		}
		double pBackground = PXGivenH(getBgMeanCVec(), getBgCovarInv(), getBgCovar(), getBgCovarDet(), val, x, y);
//		System.out.println("pForeground:"+pForeground);
//		System.out.println("pBackground:"+pBackground);
		
		double totalCount = fgCount + bgCount + borderCount;
		
		double pFG = fgCount/totalCount;
		double pBG = (bgCount + borderCount)/totalCount;
//		System.out.println("pFG:"+pFG);
//		System.out.println("pBG:"+pBG);
		
		double fgFinal = pForeground * pFG;
		double bgFinal = pBackground * pBG;
		
//		if(x > 540 && x < 590){
//			if(y > 160 && y < 200){
//				System.out.print(" pForeground: "+pForeground);
//				System.out.print(" pBackground: "+pBackground);
//				System.out.print(" pFG: "+pFG);
//				System.out.print(" pBG: "+pBG);
//				
//				System.out.print(" fgFinal: " + fgFinal);
//				System.out.println(" bgFinal: " + bgFinal);
//			}
//		}

		if(fgFinal > bgFinal){
			pClass = 255;
		} else {
			pClass = 0;
		}
		
		return pClass;
	}

}
