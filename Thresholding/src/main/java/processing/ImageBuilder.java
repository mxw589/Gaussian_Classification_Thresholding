package processing;

import ij.ImagePlus;
import ij.process.ShortProcessor;

public class ImageBuilder {

	Caller caller;
	ShortProcessor sp;
	double[][] predClasses;
	
	public ImageBuilder(Caller caller, double[][] predClasses){
		this.caller = caller;
		this.predClasses = predClasses;
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
		
		ImagePlus imageTHApplied = new ImagePlus(title + "-threshold" + ext, sp);
		imageTHApplied.setCalibration(getCaller().getTBCImg().getCalibration());
		
		getCaller().setResultantImage(imageTHApplied);
	}
}
