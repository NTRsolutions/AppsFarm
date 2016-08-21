package is.web.beans.system.status;

public class SingleDiskSpaceStatsHolder {

	private boolean mainDrive;
	private String fileSystemName;
	private String fileSystemType;
	private String sizeTotal;
	private long sizeTotalValue;
	private String sizeUsed;
	private String sizeAvailable;
	private String sizeUsagePercentage;
	private String fileSystemMountPath;
	private double sizeUsageRatio = 0.0;
	
	public String getFileSystemName() {
		return fileSystemName;
	}
	public void setFileSystemName(String fileSystemName) {
		this.fileSystemName = fileSystemName;
	}
	public String getFileSystemType() {
		return fileSystemType;
	}
	public void setFileSystemType(String fileSystemType) {
		this.fileSystemType = fileSystemType;
	}
	public String getSizeTotal() {
		return sizeTotal;
	}
	public void setSizeTotal(String sizeTotal) {
		this.sizeTotal = sizeTotal;
	}
	public String getSizeUsed() {
		return sizeUsed;
	}
	public void setSizeUsed(String sizeUsed) {
		this.sizeUsed = sizeUsed;
	}
	public String getSizeAvailable() {
		return sizeAvailable;
	}
	public void setSizeAvailable(String sizeAvailable) {
		this.sizeAvailable = sizeAvailable;
	}
	public String getSizeUsagePercentage() {
		return sizeUsagePercentage;
	}
	public void setSizeUsagePercentage(String sizeUsagePercentage) {
		this.sizeUsagePercentage = sizeUsagePercentage;
		try {
			if(!sizeUsagePercentage.equals("-") && sizeUsagePercentage.length() > 1) {
				String strVal = sizeUsagePercentage.substring(0,sizeUsagePercentage.length()-1);
				sizeUsageRatio = Double.valueOf(strVal);
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	public String getFileSystemMountPath() {
		return fileSystemMountPath;
	}
	public void setFileSystemMountPath(String fileSystemMountPath) {
		this.fileSystemMountPath = fileSystemMountPath;
	}
	public double getSizeUsageRatio() {
		return sizeUsageRatio;
	}
	public void setSizeUsageRatio(double sizeUsageRatio) {
		this.sizeUsageRatio = sizeUsageRatio;
	}
	public void printStats() {
		System.out.println("=======================================");
		System.out.println("File system name: "+fileSystemName);
		System.out.println("File system type: "+fileSystemType);
		System.out.println("File system size total: "+sizeTotal);
		System.out.println("File system size used: "+sizeUsed);
		System.out.println("File system used percentage: "+sizeUsagePercentage);
		System.out.println("File system usage ratio: "+sizeUsageRatio);
		System.out.println("File system size available: "+sizeAvailable);
		System.out.println("File system mount path: "+fileSystemMountPath);
		System.out.println("=======================================");
	}
	public boolean isMainDrive() {
		return mainDrive;
	}
	public void setMainDrive(boolean mainDrive) {
		this.mainDrive = mainDrive;
	}
	public long getSizeTotalValue() {
		return sizeTotalValue;
	}
	public void setSizeTotalValue(long sizeTotalValue) {
		this.sizeTotalValue = sizeTotalValue;
	}
	
}