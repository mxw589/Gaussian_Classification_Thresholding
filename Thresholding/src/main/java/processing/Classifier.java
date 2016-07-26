package processing;

import datatypes.PixelsValues;
import ij.IJ;

public class Classifier {

	private Caller caller;
	private double[] fgMeans;
	private double[] bgMeans;
	private double[][] covariance;
	double bgCount;
	double fgCount;
	double borderCount;

	public Classifier(Caller caller){
		this.caller = caller;
		int nSq = caller.getNeighbours() * caller.getNeighbours();
		this.fgMeans = new double[nSq];
		this.bgMeans = new double[nSq];
		this.covariance = new double[nSq][nSq];
	}

	public double[] getFgMeans() {
		return fgMeans;
	}

	public double[] getBgMeans() {
		return bgMeans;
	}

	public double[][] getCovariance() {
		return covariance;
	}

	public void setCovariance(double[][] covariance) {
		this.covariance = covariance;
	}

	public Caller getCaller() {
		return caller;
	}

	public void calculateMeans(){
		PixelsValues[][] readImage = getCaller().getReadImage();

		int nSq = caller.getNeighbours() * getCaller().getNeighbours();

		double[] bgTotal = new double[nSq];
		double[] fgTotal = new double[nSq];

		int step = caller.getNeighbours()/2;
		IJ.log("" + step);

		for(int widthP = 0 + step; widthP < getCaller().IMAGE_WIDTH - step; widthP++){
			for(int heightP = 0 + step; heightP  < getCaller().IMAGE_HEIGHT - step; heightP ++){
				if(readImage[widthP][heightP].getMaskVal().equals("foreground")){
					fgCount++;
					totalWindow(fgTotal, widthP, heightP, step);
				} else if(readImage[widthP][heightP].getMaskVal().equals("background") ||
						readImage[widthP][heightP].getMaskVal().equals("border")){
					bgCount++;
					totalWindow(bgTotal, widthP, heightP, step);
				}
			}
		}
		
		for(int x = 0; x < nSq; x++){
			getFgMeans()[x] = fgTotal[x] / fgCount;
			getBgMeans()[x] = bgTotal[x] / bgCount;
			IJ.log("" + getFgMeans()[x]);
			IJ.log("" + getBgMeans()[x]);
		}
	}

	public void totalWindow(double[] totals, int x, int y, int step){
		PixelsValues[][] readImage = getCaller().getReadImage();
		
		for(int windowY = 0; windowY < getCaller().getNeighbours(); windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				totals[windowY * caller.getNeighbours() + windowX] += 
						readImage[x - step + windowX][y - step + windowY].getValue();
			}
		}
	}

}
