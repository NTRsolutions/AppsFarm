package is.ejb.bl.monitoring.server;

import java.util.ArrayList;

public class ServerStats {

	private String hostName;
	private String hostIpAddress; 

	private double memTotalUsedPercentage;
	private long memUsed;
	private long memJavaTotalAllocated; 
	private double memJavaUsageRatio;
	private double cpuUtilisation;

	//------GC------------
	private long gcCount;
	private long gcTime;
	
	// Build up RRD statistics
	private double requestsCWMP;
	private double requestsDB;
	private double requestsMonitoring;
				
	//network IO
	private double netDownload;
	private double netUpload; 
					
	private double logsWarning;
	private double logsSevere;

	//disk IO
	private double diskReads;
	private double diskWrites;
	
	//main disk stats
	private SingleDiskSpaceStatsHolder diskStats;
	
	//all disks stats
	ArrayList<SingleDiskSpaceStatsHolder> disksStats;
	
	//server info
	private String currentTime;
	private String upTime;
	private String loadAverage;
	private String currentFqdn;
	private String javaVendor;
	private String javaVersion;
	private String osArch;
	private String osName;
	private String osCodeName;
	private String osMachine;
	private String osVendor;
	private String osVendorVersion;
	private String osPatchLevel;
	
	//network info
	private String networkDefaultGateway;
	private String networkDomainName;
	private String networkHostName;
	private String networkPrimaryDns;
	private String networkSecondaryDns;
	private String networkPrimaryInterface;
	private String networkPrimaryIpAddress;
	private String networkPrimaryMacAddress;
	private String networkPrimaryNetmask;

	public double getMemTotalUsedPercentage() {
		return memTotalUsedPercentage;
	}
	public void setMemTotalUsedPercentage(double memTotalUsedPercentage) {
		this.memTotalUsedPercentage = memTotalUsedPercentage;
	}
	public long getMemUsed() {
		return memUsed;
	}
	public void setMemUsed(long memUsed) {
		this.memUsed = memUsed;
	}
	public long getMemJavaTotalAllocated() {
		return memJavaTotalAllocated;
	}
	public void setMemJavaTotalAllocated(long memJavaTotalAllocated) {
		this.memJavaTotalAllocated = memJavaTotalAllocated;
	}
	public double getMemJavaUsageRatio() {
		return memJavaUsageRatio;
	}
	public void setMemJavaUsageRatio(double memJavaUsageRatio) {
		this.memJavaUsageRatio = memJavaUsageRatio;
	}
	public double getCpuUtilisation() {
		return cpuUtilisation;
	}
	public void setCpuUtilisation(double cpuUtilisation) {
		this.cpuUtilisation = cpuUtilisation;
	}
	public long getGcCount() {
		return gcCount;
	}
	public void setGcCount(long gcCount) {
		this.gcCount = gcCount;
	}
	public long getGcTime() {
		return gcTime;
	}
	public void setGcTime(long gcTime) {
		this.gcTime = gcTime;
	}
	public double getRequestsCWMP() {
		return requestsCWMP;
	}
	public void setRequestsCWMP(double requestsCWMP) {
		this.requestsCWMP = requestsCWMP;
	}
	public double getRequestsDB() {
		return requestsDB;
	}
	public void setRequestsDB(double requestsDB) {
		this.requestsDB = requestsDB;
	}
	public double getRequestsMonitoring() {
		return requestsMonitoring;
	}
	public void setRequestsMonitoring(double requestsMonitoring) {
		this.requestsMonitoring = requestsMonitoring;
	}
	public double getNetDownload() {
		return netDownload;
	}
	public void setNetDownload(double netDownload) {
		this.netDownload = netDownload;
	}
	public double getNetUpload() {
		return netUpload;
	}
	public void setNetUpload(double netUpload) {
		this.netUpload = netUpload;
	}
	
	public double getLogsWarning() {
		return logsWarning;
	}
	public void setLogsWarning(double logsWarning) {
		this.logsWarning = logsWarning;
	}
	public double getLogsSevere() {
		return logsSevere;
	}
	public void setLogsSevere(double logsSevere) {
		this.logsSevere = logsSevere;
	}
	public double getDiskReads() {
		return diskReads;
	}
	public void setDiskReads(double diskReads) {
		this.diskReads = diskReads;
	}
	public double getDiskWrites() {
		return diskWrites;
	}
	public void setDiskWrites(double diskWrites) {
		this.diskWrites = diskWrites;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostIpAddress() {
		return hostIpAddress;
	}
	public void setHostIpAddress(String hostIpAddress) {
		this.hostIpAddress = hostIpAddress;
	}
	public SingleDiskSpaceStatsHolder getDiskStats() {
		return diskStats;
	}
	public void setDiskStats(SingleDiskSpaceStatsHolder diskStats) {
		this.diskStats = diskStats;
	}
	public ArrayList<SingleDiskSpaceStatsHolder> getDisksStats() {
		return disksStats;
	}
	public void setDisksStats(ArrayList<SingleDiskSpaceStatsHolder> disksStats) {
		this.disksStats = disksStats;
	}
	public String getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}
	public String getUpTime() {
		return upTime;
	}
	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}
	public String getLoadAverage() {
		return loadAverage;
	}
	public void setLoadAverage(String loadAverage) {
		this.loadAverage = loadAverage;
	}
	public String getCurrentFqdn() {
		return currentFqdn;
	}
	public void setCurrentFqdn(String currentFqdn) {
		this.currentFqdn = currentFqdn;
	}
	public String getJavaVendor() {
		return javaVendor;
	}
	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}
	public String getJavaVersion() {
		return javaVersion;
	}
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	public String getOsArch() {
		return osArch;
	}
	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}
	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
	public String getOsCodeName() {
		return osCodeName;
	}
	public void setOsCodeName(String osCodeName) {
		this.osCodeName = osCodeName;
	}
	public String getOsMachine() {
		return osMachine;
	}
	public void setOsMachine(String osMachine) {
		this.osMachine = osMachine;
	}
	public String getOsVendor() {
		return osVendor;
	}
	public void setOsVendor(String osVendor) {
		this.osVendor = osVendor;
	}
	public String getOsVendorVersion() {
		return osVendorVersion;
	}
	public void setOsVendorVersion(String osVendorVersion) {
		this.osVendorVersion = osVendorVersion;
	}
	public String getOsPatchLevel() {
		return osPatchLevel;
	}
	public void setOsPatchLevel(String osPatchLevel) {
		this.osPatchLevel = osPatchLevel;
	}
	public String getNetworkDefaultGateway() {
		return networkDefaultGateway;
	}
	public void setNetworkDefaultGateway(String networkDefaultGateway) {
		this.networkDefaultGateway = networkDefaultGateway;
	}
	public String getNetworkDomainName() {
		return networkDomainName;
	}
	public void setNetworkDomainName(String networkDomainName) {
		this.networkDomainName = networkDomainName;
	}
	public String getNetworkHostName() {
		return networkHostName;
	}
	public void setNetworkHostName(String networkHostName) {
		this.networkHostName = networkHostName;
	}
	public String getNetworkPrimaryDns() {
		return networkPrimaryDns;
	}
	public void setNetworkPrimaryDns(String networkPrimaryDns) {
		this.networkPrimaryDns = networkPrimaryDns;
	}
	public String getNetworkSecondaryDns() {
		return networkSecondaryDns;
	}
	public void setNetworkSecondaryDns(String networkSecondaryDns) {
		this.networkSecondaryDns = networkSecondaryDns;
	}
	public String getNetworkPrimaryInterface() {
		return networkPrimaryInterface;
	}
	public void setNetworkPrimaryInterface(String networkPrimaryInterface) {
		this.networkPrimaryInterface = networkPrimaryInterface;
	}
	public String getNetworkPrimaryIpAddress() {
		return networkPrimaryIpAddress;
	}
	public void setNetworkPrimaryIpAddress(String networkPrimaryIpAddress) {
		this.networkPrimaryIpAddress = networkPrimaryIpAddress;
	}
	public String getNetworkPrimaryMacAddress() {
		return networkPrimaryMacAddress;
	}
	public void setNetworkPrimaryMacAddress(String networkPrimaryMacAddress) {
		this.networkPrimaryMacAddress = networkPrimaryMacAddress;
	}
	public String getNetworkPrimaryNetmask() {
		return networkPrimaryNetmask;
	}
	public void setNetworkPrimaryNetmask(String networkPrimaryNetmask) {
		this.networkPrimaryNetmask = networkPrimaryNetmask;
	}
	
}
