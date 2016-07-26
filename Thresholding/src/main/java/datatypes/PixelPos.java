package datatypes;


public class PixelPos{
	private int x;
	private int y;
	
	public PixelPos(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public String toString(){
		return "" + x + "," + y;
	}

	public boolean equals(PixelPos o) {
		boolean checkX = false;
		boolean checkY = false;
		
		if(o.getX() == this.getX()){
			checkX = true;
		}
		
		if(o.getY() == this.getY()){
			checkY = true;
		}
		
		return checkX && checkY;
	}
}
