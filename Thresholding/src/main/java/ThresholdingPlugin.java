import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import processing.Caller;
/**
 * Class that initialises the plugin. Establishes the GUI elements for
 * ImageJ and also contains a main method for execution in Eclipse for
 * debugging purposes
 * @author Mark
 *
 */
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
					"ERROR: At least two images needs to be open to run a threshold.");
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
		
		gd.addChoice("Image to build classifier", imgNames, imgNames[0]);
		gd.addChoice("Mask to build classifier", imgNames, imgNames[1]);
		gd.addChoice("Image to threshold", imgNames, imgNames[0]);
		gd.addNumericField("Classifier structuring element width and height", 5, 0);
		gd.addNumericField("Top-hat structuring element radius", 25, 0);
		gd.showDialog();
		
		if(gd.wasOKed()){
			ImagePlus image = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			ImagePlus mask = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			ImagePlus tbcImg = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			int neighbours = (int) gd.getNextNumber();
			int radius = (int) gd.getNextNumber();
			
			ImagePlus result = process(image, mask, tbcImg, neighbours,radius);
			
//			result.show();
		}
	}
	/**
	 * Creates the caller object that will initialise the process of
	 * the image processing
	 * @param image the training image
	 * @param mask the binary mask for the training image
	 * @param TBCImg the image to be classified
	 * @param neighbours the nxn size of the classifier structuring element
	 * @param radius the radius to be used in the top hat transform
	 * @return the image formed as a result of the classification
	 */
	public ImagePlus process(ImagePlus image, ImagePlus mask, ImagePlus TBCImg, int neighbours,int radius){
		Caller caller = new Caller(image, mask, TBCImg, neighbours,radius);
		
		caller.call();
		
		return caller.getResultantImage();
	}
	
	
	/*
	 * main method for starting the project for debugging
	 */
	public static void main(String[] args) {
		new ImageJ();
		
//	    ImagePlus image6 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/test.tif");
//	    image6.show();
//	    ImagePlus image7 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/test-mask.tif");
//	    image7.show();
		ImagePlus image0 = IJ.openImage("/Users/Mark/Documents/Project/Images/1_FITC.tif");
	    image0.show();
	    ImagePlus image1 = IJ.openImage("/Users/Mark/Documents/Project/Images/1_FITC-mask.tif");
	    image1.show();
	    ImagePlus image2 = IJ.openImage("/Users/Mark/Documents/Project/Images/2_FITC.tif");
	    image2.show();
	    ImagePlus image7 = IJ.openImage("/Users/Mark/Documents/Project/Images/3_FITC.tif");
	    image7.show();
	    ImagePlus image8 = IJ.openImage("/Users/Mark/Documents/Project/Images/4_FITC.tif");
	    image8.show();
	    ImagePlus image3 = IJ.openImage("/Users/Mark/Documents/Project/Images/5_FITC.tif");
	    image3.show();
		ImagePlus image4 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/6_FITC-mask.tif");
	    image4.show();
	    ImagePlus image5 = IJ.openImage("/Users/Mark/Documents/Project/Images/6_FITC.tif");
	    image5.show();
	    
	    IJ.runPlugIn("ThresholdingPlugin", "");
	}

}
