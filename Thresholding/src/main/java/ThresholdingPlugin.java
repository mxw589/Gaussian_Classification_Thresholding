import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import processing.Caller;

public class ThresholdingPlugin implements PlugIn{

	/**
	 * main run method, this will create the relevant user dialogs and begin
	 * execution of the project
	 */
	@Override
	public void run(String arg) {
		/*
		 * count images to be processed
		 */
		int imgCount = WindowManager.getImageCount();
		
		/*
		 * error if there are no images to be used
		 */
		if( imgCount < 2 ){
			IJ.error( "WaterShed Test", 
					"ERROR: At least two images needs to be open to run a watershed.");
			return;
		}
		
		/*
		 * save image names to an array 
		 */
		String[] imgNames = new String[imgCount];
		
		for(int i = 0; i < imgCount; i++){
			imgNames[i] = WindowManager.getImage(i + 1).getShortTitle();
		}
		
		/*
		 * create a dialog box for initiating the watershed operation
		 */
		GenericDialog gd = new GenericDialog("Watershed");
		
		gd.addChoice("Image", imgNames, imgNames[0]);
		gd.addChoice("Mask", imgNames, imgNames[1]);
		gd.addNumericField("Neighbour grid size", 3, 1);
		gd.showDialog();
		
		if(gd.wasOKed()){
			ImagePlus image = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			ImagePlus mask = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			int neighbours = (int) gd.getNextNumber();
			
			ImagePlus result = process(image, mask, neighbours);
			
//			result.show();
		}
	}
	
	public ImagePlus process(ImagePlus image, ImagePlus mask, int neighbours){
		Caller caller = new Caller(image, mask, neighbours);
		
		caller.call();
		
		return caller.getResultantImage();
	}
	
	
	/*
	 * main method for starting the project for debugging
	 */
	public static void main(String[] args) {
		new ImageJ();
		
		ImagePlus image0 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/6_FITC-sample-8-bit.tif");
	    image0.show();
	    ImagePlus image1 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/6_FITC-sample-mask.tif");
	    image1.show();
	    IJ.runPlugIn("ThresholdingPlugin", "");
	}

}
