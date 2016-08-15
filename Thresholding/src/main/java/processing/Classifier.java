package processing;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import datatypes.PixelPos;
import datatypes.PixelsValues;

public class Classifier {

	private Caller caller;
	private double[][] fgMeans;
	private double[][] bgMeans;
	private double[][] fgCovariance;
	private double[][] bgCovariance;
	private boolean validation;
	private PixelPos valiPixel;
	private RealMatrix fgMeanCVec;
	private RealMatrix fgCovarInv;
	private double fgCovarDet;
	private RealMatrix bgMeanCVec;
	private RealMatrix bgCovarInv;
	private double bgCovarDet;
	double bgCount;
	double fgCount;
	double borderCount;

	public Classifier(Caller caller){
		this.caller = caller;
		this.fgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
		this.bgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
		this.validation = false;
	}
	
	/**
	 * special classifier constructor for validation purposes. You are able to leave
	 * the co-ordinate of a pixel that you want to leave out of the contruction of the
	 * data set.
	 * @param caller
	 * @param x
	 * @param y
	 */
	public Classifier(Caller caller, int x, int y){
		this.caller = caller;
		this.fgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
		this.bgMeans = new double[caller.getNeighbours()][caller.getNeighbours()];
		this.validation = true;
		this.valiPixel = new PixelPos(x,y);
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
	
	public boolean isValidation() {
		return validation;
	}

	public PixelPos getValiPixel() {
		return valiPixel;
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

	public void calculateMeans(){
		PixelsValues[][] readImage = getCaller().getReadImage();

		double[][] bgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];
		double[][] fgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];

		int step = getCaller().getNeighbours()/2;


		for(int heightP = 0 + step; heightP  < getCaller().IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < getCaller().IMAGE_WIDTH - step; widthP++){
				if(!isValidation() || heightP != getValiPixel().getY() || widthP != getValiPixel().getX()){
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

	public double[][] calcEXY(String layer){
		PixelsValues[][] readImage = getCaller().getReadImage();

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		double[][] eXY = new double[nSq][nSq];
		int setCounter = 0;

		int step = getCaller().getNeighbours()/2;
		

		for(int heightP = 0 + step; heightP  < getCaller().IMAGE_HEIGHT - step; heightP ++){
			for(int widthP = 0 + step; widthP < getCaller().IMAGE_WIDTH - step; widthP++){
				if(!isValidation() || heightP != getValiPixel().getY() || widthP != getValiPixel().getX()){
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

	public void totalWindow(double[][] totals, int x, int y){
		PixelsValues[][] readImage = getCaller().getReadImage();
		
		int adjust = getCaller().getNeighbours() / 2;

		for(int windowY = 0; windowY < getCaller().getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				totals[windowX][windowY] += readImage[x + windowX - adjust][y + windowY - adjust].getValue();
				
//				//Debugging
//				if(windowY ==0 && windowX == 0){
//					System.out.println("TL:" + readImage[x + windowX - adjust][y + windowY - adjust].getValue());
//				}
//				if(windowY ==0 && windowX == 1){
//					System.out.println("TM:" + readImage[x + windowX - adjust][y + windowY - adjust].getValue());
//				}
			}
		}
	}
	
	public void calcDetInv(){
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		RealMatrix covarMatrix = new Array2DRowRealMatrix(getFgCovariance());
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
		ludecomp = new LUDecomposition(covarMatrix);
//		System.out.println(covarMatrix.getColumnDimension() + ", " + covarMatrix.getRowDimension());
//		System.out.println(covarMatrix);
		covarDet = ludecomp.getDeterminant();
		setBgCovarDet(covarDet);
//		System.out.println(covarDet);
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
	
	public double PXGivenH(RealMatrix meanCVec, RealMatrix covarInv, double covarDet, double[] val){
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
//		System.out.println(meanCVec);
		
		RealMatrix valCVec = new Array2DRowRealMatrix(val);
//		System.out.println(valCVec);
		
		RealMatrix valMinusMean = valCVec.subtract(meanCVec);
//		System.out.println(valMinusMean);
		
		return (1/Math.sqrt(Math.pow(2*Math.PI, nSq)*covarDet)) * Math.exp(-0.5 * (valMinusMean.transpose().multiply(covarInv)).multiply(valMinusMean).getEntry(0, 0));
	}
	
	public double predictedClass(double[] val){
		double pClass;
		
		double pForeground = PXGivenH(getFgMeanCVec(), getFgCovarInv(), getFgCovarDet(), val);
		double pBackground = PXGivenH(getBgMeanCVec(), getBgCovarInv(), getBgCovarDet(), val);
//		System.out.println("pBackground:"+pBackground);
		
		double totalCount = fgCount + bgCount + borderCount;
		
		double pFG = fgCount/totalCount;
		double pBG = (bgCount + borderCount)/totalCount;
//		System.out.println("pBG:"+pBG);
		
		double fgFinal = pForeground * pFG;
		double bgFinal = pBackground * pBG;
		
		
//		System.out.println("fgFinal: " + fgFinal);
//		System.out.println("bgFinal: " + bgFinal);
		if(fgFinal > bgFinal){
			pClass = 1;
		} else {
			pClass = 0;
		}
		
		return pClass;
	}

}
