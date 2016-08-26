package is.ejb.bl.acra;

public class AcraDisplay {
	private int height;
	private int orientation;
	private int pixelFormat;
	private double refreshRate;
	private int width;
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getOrientation() {
		return orientation;
	}
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	public int getPixelFormat() {
		return pixelFormat;
	}
	public void setPixelFormat(int pixelFormat) {
		this.pixelFormat = pixelFormat;
	}
	public double getRefreshRate() {
		return refreshRate;
	}
	public void setRefreshRate(double refreshRate) {
		this.refreshRate = refreshRate;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	@Override
	public String toString() {
		return "AcraDisplay [height=" + height + ", orientation=" + orientation + ", pixelFormat=" + pixelFormat
				+ ", refreshRate=" + refreshRate + ", width=" + width + "]";
	}
	
	
}
