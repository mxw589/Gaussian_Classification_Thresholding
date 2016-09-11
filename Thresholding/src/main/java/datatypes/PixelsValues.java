package datatypes;
/**
 * data type for a pixels position and its greyscale brightness within
 * a training image. With this in mind the label of the pixel with
 * regards to the training mask is included
 *
 * @author Mark
 *
 */
public class PixelsValues implements Comparable<PixelsValues>{

	private double value;
	private PixelPos pixelPos;
	private int number;
	private String maskVal;

	/**
	 * Constructor for the class
	 * @param pixelPos the position of the pixel in the image
	 * @param value the brightness value of the pixel
	 * @param number the number of the pixel within the image
	 * @param maskVal the value of the training mask at the pixels
	 * location in the training image
	 */
	public PixelsValues(PixelPos pixelPos, double value, int number, String maskVal){
		this.value = value;
		this.pixelPos = pixelPos;
		this.number = number;
		this.maskVal = maskVal;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public PixelPos getPixelPos() {
		return pixelPos;
	}

	public int getNumber() {
		return number;
	}

	public String getMaskVal() {
		return maskVal;
	}

	public void setMaskVal(String maskVal) {
		this.maskVal = maskVal;
	}

	/**
	 * @param o the PixelsValues object to for comparison
	 * @return an integer value that indicates whether a pixel is lighter or darker
	 * than another
	 */
	public int compareTo(PixelsValues o) {
		int returnVal = Double.compare(getValue(), o.getValue());
		if( returnVal == 0 ){
			if(getNumber() < o.getNumber()){
				return 1;
			} else {
				return -1;
			}
		}
		return returnVal;
	}

}
