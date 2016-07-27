package processing;

import datatypes.PixelsValues;
import ij.IJ;

public class Classifier {

	private Caller caller;
	private double[][] fgMeans;
	private double[][] bgMeans;
	private double[][] fgCovariance;
	private double[][] bgCovariance;
	double bgCount;
	double fgCount;
	double borderCount;

	public Classifier(Caller caller){
		this.caller = caller;
		int nSq = caller.getNeighbours() * caller.getNeighbours();
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

	public void calculateMeans(){
		PixelsValues[][] readImage = getCaller().getReadImage();

		double[][] bgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];
		double[][] fgTotal = new double[getCaller().getNeighbours()][getCaller().getNeighbours()];

		int step = caller.getNeighbours()/2;


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
		for(int y = 0; y < getCaller().getNeighbours(); y++){
			for(int x = 0; x < getCaller().getNeighbours(); x++){
				getFgMeans()[x][y] = fgTotal[x][y] / fgCount;
				getBgMeans()[x][y] = bgTotal[x][y] / bgCount;
			}
		}
	}

	public void calculateCovariance(){

		double[][] fgeXeY = calcEXEY(getFgMeans());
		double[][] fgeXY = calcEXY(getFgMeans(), "foreground");

		double[][] bgeXeY = calcEXEY(getBgMeans());
		double[][] bgeXY = calcEXY(getBgMeans(), "background");

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		
		double[][] fgCov = new double[nSq][nSq];
		double[][] bgCov = new double[nSq][nSq];
	

		for(int windowY = 0; windowY < nSq; windowY++){
			for(int windowX = 0; windowX < getCaller().getNeighbours(); windowX++){
				fgCov[windowX][windowY] = fgeXY[windowX][windowY] - fgeXeY[windowX][windowY];
				bgCov[windowX][windowY] = bgeXY[windowX][windowY] - bgeXeY[windowX][windowY];
			}
		}
		
		setFgCovariance(fgCov);
		setBgCovariance(bgCov);
	}

	public double[][] calcEXEY(double[][] means){
		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();

		double[][] eXeY = new double[nSq][nSq];

		for(int windowY = 0; windowY < nSq; windowY++){
			int meanX = windowY % getCaller().getNeighbours();
			int meanY = windowY / getCaller().getNeighbours();

			double rowMean = means[meanX][meanY];

			for(int windowX = 0; windowX < nSq; windowX++){
				int meanXX = windowX % getCaller().getNeighbours();
				int meanXY = windowX / getCaller().getNeighbours();

				eXeY[windowX][windowY] = rowMean * means[meanXX][meanXY];
			}
		}

		return eXeY;
	}

	public double[][] calcEXY(double[][] means, String layer){
		PixelsValues[][] readImage = getCaller().getReadImage();

		int nSq = getCaller().getNeighbours() * getCaller().getNeighbours();
		double[][] eXY = new double[nSq][nSq];
		int setCounter = 0;

		int step = caller.getNeighbours()/2;
		

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
		
		for(int windowY = 0; windowY < nSq; windowY++){
			for(int windowX = 0; windowX < nSq; windowX++){
				eXY[windowX][windowY] = eXY[windowX][windowY] / setCounter;
			}
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
			}
		}
	}

}
