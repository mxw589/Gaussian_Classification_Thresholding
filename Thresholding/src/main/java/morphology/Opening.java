package morphology;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import datatypes.PixelPos;
import processing.Caller;
import processing.ImageBuilder;

public class Opening {

	public static double[][] open(Caller caller, Set<PixelPos> structuringElement, double[][] image, int width, int height){
		
		ExecutorService erosionTaskExecutor = Executors.newFixedThreadPool(4);
		
		double[][] eroded = new double[width][height];
		ErosionThread[][] erosionThreads = new ErosionThread[width][height];
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				erosionThreads[x][y] = new ErosionThread(structuringElement, image, width, height);
				erosionThreads[x][y].setX(x);
				erosionThreads[x][y].setY(y);
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				erosionTaskExecutor.execute(erosionThreads[x][y]);
			}
		}
		
		erosionTaskExecutor.shutdown();
		
		try{
			erosionTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}catch(InterruptedException e){
			System.out.println("err1");
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				eroded[x][y] = erosionThreads[x][y].getOutputPixel();
			}
		}
		
		ExecutorService dilationTaskExecutor = Executors.newFixedThreadPool(4);
		
		double[][] dilated = new double[width][height];
		DilationThread[][] dilationThreads = new DilationThread[width][height];
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				dilationThreads[x][y] = new DilationThread(structuringElement, eroded, width, height);
				dilationThreads[x][y].setX(x);
				dilationThreads[x][y].setY(y);
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				dilationTaskExecutor.execute(dilationThreads[x][y]);
			}
		}
		
		dilationTaskExecutor.shutdown();
		
		try{
			dilationTaskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}catch(InterruptedException e){
			System.out.println("err2");
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				dilated[x][y] = dilationThreads[x][y].getOutputPixel();
			}
		}
		
//		ImageBuilder imageBuilder4 = new ImageBuilder(caller, dilated, "background");
//		imageBuilder4.buildImage();
//		caller.getResultantImage().show();
		return dilated;
	}

}
