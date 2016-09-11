package processing;

import ij.ImagePlus;
import ij.process.ShortProcessor;
/**
 * a class that builds an image into a format that ImageJ can display
 * @author Mark
 *
 */
public class ImageBuilder {

	Caller caller;
	ShortProcessor sp;
	double[][] predClasses;
	String title;
	
	/**
	 * the constructor for the class
	 * @param caller the calling object
	 * @param predClasses the brightness values to be present in the
	 * constructed image
	 * @param title the title of the image
	 */
	public ImageBuilder(Caller caller, double[][] predClasses, String title){
		this.caller = caller;
		this.predClasses = predClasses;
		this.title = title;
	}
	
	public ShortProcessor getSp() {
		return sp;
	}

	public void setSp(ShortProcessor sp) {
		this.sp = sp;
	}

	public double[][] getPredClasses() {
		return predClasses;
	}

	public void setPredClasses(double[][] predClasses) {
		this.predClasses = predClasses;
	}

	public Caller getCaller() {
		return caller;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * the method that builds the brightness into a format that ImageJ
	 * understands. They are stored in the resultantImage variable of the
	 * calling class
	 */
	public void buildImage(){
		int width = getCaller().TBC_IMAGE_WIDTH;
		int height = getCaller().TBC_IMAGE_HEIGHT;
		
		ShortProcessor sp = new ShortProcessor(width, height);
		
		for(int widthFP = 0; widthFP < width; widthFP++){
			for(int heightFP = 0; heightFP < height; heightFP++){
				sp.set(widthFP, heightFP, (int) getPredClasses()[widthFP][heightFP]);
			}
		}
		
		String title = getCaller().getTBCImg().getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring(index);
			title = title.substring(0, index);				
		}
		
		ImagePlus imageTHApplied = new ImagePlus(title + "-" + getTitle() + ext, sp);
		imageTHApplied.setCalibration(getCaller().getTBCImg().getCalibration());
		
		getCaller().setResultantImage(imageTHApplied);
	}
}
