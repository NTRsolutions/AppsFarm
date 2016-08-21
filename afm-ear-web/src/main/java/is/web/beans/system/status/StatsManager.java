package is.web.beans.system.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.shell.ShellCommandExecException;
import org.hyperic.sigar.shell.ShellCommandUsageException;

public class StatsManager {

	private static Logger logger = Logger.getLogger("StatsManager");

	private StatsGeneric cpuInfo = new StatsGeneric();
	private StatsDisksIO diskInfo = new StatsDisksIO();
	private StatsUpTime uptimeInfo = new StatsUpTime();
	private StatsVersion versionInfo = new StatsVersion();
	private StatsDiskSpace diskSpaceInfo = new StatsDiskSpace();
	
	private long totalDiskReads = 0;
	private long totalDiskWrites = 0;
	
	private long totalDiskReadBytes = 0;
	private long totalDiskWriteBytes = 0;

	public StatsManager()
	{
		logger.info("NodeStats Manager initialised...");
		//System.loadLibrary("libsigar-amd64-linux");
		//for testing
		regenerateStats();
	}
	
	public void regenerateStats() {
		//reset these values
		totalDiskReads = 0;
		totalDiskWrites = 0;
		totalDiskReadBytes = 0;
		totalDiskWriteBytes = 0;

		initCpuInfo();
		initDiskInfo();
		initUptimeInfo();
		initVersionInfo();
		initDiskSpaceInfo();
		initUptimeInfo();

		//set generic stats
//		sdHolder.setStatsCpuModelName(cpuInfo.getStatsCpuModelName().trim());
//		sdHolder.setStatsCpuModelSpeed(cpuInfo.getStatsCpuModelSpeed());
//		sdHolder.setStatsCpuCacheSize(cpuInfo.getStatsCpuCacheSize());
//		sdHolder.setStatsPhysicalCpus(cpuInfo.getStatsPhysicalCpus());
//		sdHolder.setStatsNumberOfCores(cpuInfo.getStatsNumberOfCores());
//		sdHolder.setStatsCpuUtilisation(cpuInfo.getStatsCpuUtilisation().trim());
//		
//		sdHolder.setStatsMemTotal(cpuInfo.getStatsMemTotal());
//		sdHolder.setStatsMemUsed(cpuInfo.getStatsMemUsed());
//		sdHolder.setStatsMemFree(cpuInfo.getStatsMemFree());
//
//		double memUsedPercentageVal = ((double)cpuInfo.getStatsMemUsed()/(double)cpuInfo.getStatsMemTotal()) * (double)100;
//		String memUsagePercentage = memUsedPercentageVal+"";
//		memUsagePercentage = memUsagePercentage.substring(0,4)+"%";
//		sdHolder.setStatsMemUsedPercentage(memUsagePercentage.trim());
//		logger.info("mem usage percentage: "+sdHolder.getStatsMemUsedPercentage());
//		sdHolder.setMemUsedRatio();
//		
//		//network I/O stats
//		sdHolder.setStatsNetworkDownloadSpeed(cpuInfo.getStatsNetworkDownloadSpeed().trim());
//		sdHolder.setStatsNetworkUploadSpeed(cpuInfo.getStatsNetworkUploadSpeed().trim());
//
//	    //network info
//		sdHolder.setStatsNetworkPrimaryInterface(cpuInfo.getStatsNetworkPrimaryInterface().trim());
//		sdHolder.setStatsNetworkPrimaryIpAddress(cpuInfo.getStatsNetworkPrimaryIpAddress().trim());
//		sdHolder.setStatsNetworkPrimaryMacAddress(cpuInfo.getStatsNetworkPrimaryMacAddress().trim());
//		sdHolder.setStatsNetworkPrimaryNetmask(cpuInfo.getStatsNetworkPrimaryNetmask().trim());
//		sdHolder.setStatsNetworkHostName(cpuInfo.getStatsNetworkHostName().trim());
//		sdHolder.setStatsNetworkDomainName(cpuInfo.getStatsNetworkDomainName().trim());
//		sdHolder.setStatsNetworkDefaultGateway(cpuInfo.getStatsNetworkDefaultGateway().trim());
//		sdHolder.setStatsNetworkPrimaryDns(cpuInfo.getStatsNetworkPrimaryDns().trim());
//		sdHolder.setStatsNetworkSecondaryDns(cpuInfo.getStatsNetworkSecondaryDns().trim());
//		
//		//set disk I/O stats
//		sdHolder.setStatsTotalDiskReadBytes(getTotalDiskReadBytes().trim());
//		sdHolder.setStatsTotalDiskWriteBytes(getTotalDiskWriteBytes().trim());
//		sdHolder.setStatsTotalDiskReads(getTotalDiskReads().trim());
//		sdHolder.setStatsTotalDiskWrites(getTotalDiskWrites().trim());
//		
//		//set disks space stats
//		sdHolder.setListDiskStats(diskSpaceInfo.getListDiskStats());
//		sdHolder.setNodeMainDisk(diskSpaceInfo.getNodeMainDisk());
//		diskSpaceInfo.setListDiskStats(new ArrayList<SingleDiskSpaceStatsHolder>());
//		
//		//set uptime stats
//		sdHolder.setStatsCurrentTime(uptimeInfo.getStatsCurrentTime().trim());
//		sdHolder.setStatsUpTime(uptimeInfo.getStatsUpTime().trim());
//		sdHolder.setStatsLoadAverage(uptimeInfo.getStatsLoadAverage().trim());
//
//		//set version stats
//		sdHolder.setStatsCurrentFqdn(versionInfo.getStatsCurrentFqdn().trim());
//		sdHolder.setStatsCurrentUser(versionInfo.getStatsCurrentUser().trim());
//		sdHolder.setStatsOSDescription(versionInfo.getStatsOSDescription().trim());
//		sdHolder.setStatsOSName(versionInfo.getStatsOSName().trim());
//		sdHolder.setStatsOSArch(versionInfo.getStatsOSArch().trim());
//		sdHolder.setStatsOSMachine(versionInfo.getStatsOSMachine().trim());
//		sdHolder.setStatsOSVersion(versionInfo.getStatsOSVersion().trim());
//		sdHolder.setStatsOSPatchLevel(versionInfo.getStatsOSPatchLevel().trim());
//		sdHolder.setStatsOSVendor(versionInfo.getStatsOSVendor().trim());
//		sdHolder.setStatsOSVendorVersion(versionInfo.getStatsOSVendorVersion().trim());
//		sdHolder.setStatsOSCodeName(versionInfo.getStatsOSCodeName().trim());
//		sdHolder.setStatsJavaVersion(versionInfo.getStatsJavaVersion().trim());
//		sdHolder.setStatsJavaVendor(versionInfo.getStatsJavaVendor().trim());
//		sdHolder.setStatsJavaHome(versionInfo.getStatsJavaHome().trim());
		
	}
	
	
	public void initCpuInfo()
	{
		try {
			cpuInfo.processCommand(new String[0]);
			//logger.info(cpuInfo.getStatsCpuModelName());
			//logger.info(cpuInfo.getStatsCpuModelSpeed()+"");
			//logger.info(cpuInfo.getStatsCpuCacheSize()+"");
			//logger.info(cpuInfo.getStatsPhysicalCpus()+"");
			//logger.info(cpuInfo.getStatsNumberOfCores()+"");
			
//			logger.info("CPU utilisation: "+cpuInfo.getStatsCpuUtilisation());
//			logger.info("Mem total: "+cpuInfo.getStatsMemTotal()+"");
//			logger.info("Mem used: "+cpuInfo.getStatsMemUsed()+"");
//			logger.info("Mem free: "+cpuInfo.getStatsMemFree()+"");
//			
//			logger.info("Network download speed: "+cpuInfo.getStatsNetworkDownloadSpeed());
//			logger.info("Network upload: "+cpuInfo.getStatsNetworkUploadSpeed());
//
//			//set disk I/O stats
//			logger.info("Total disk read bytes: "+getTotalDiskReadBytes().trim());
//			logger.info("Total disk write bytes: "+getTotalDiskWriteBytes().trim());
//			logger.info("Total disk reads: "+getTotalDiskReads().trim());
//			logger.info("Total disk writes: "+getTotalDiskWrites().trim());
			
			//set disks space stats
//			sdHolder.setListDiskStats(diskSpaceInfo.getListDiskStats());
//			sdHolder.setNodeMainDisk(diskSpaceInfo.getNodeMainDisk());
//			diskSpaceInfo.setListDiskStats(new ArrayList<SingleDiskSpaceStatsHolder>());

			
		} catch (ShellCommandUsageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandExecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initUptimeInfo()
	{
		try {
			uptimeInfo.processCommand(new String[0]);
//			logger.info(uptimeInfo.getStatsCurrentTime());
//			logger.info(uptimeInfo.getStatsUpTime());
//			logger.info(uptimeInfo.getStatsLoadAverage());
		} catch (ShellCommandUsageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandExecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initDiskInfo() {
		try {
			//read disk I/O
			diskInfo = new StatsDisksIO();
			diskInfo.processCommand(new String[0]);
			
			totalDiskReads = diskInfo.getTotalDiskReads();
			totalDiskWrites = diskInfo.getTotalDiskWrites();
			
			totalDiskReadBytes = diskInfo.getTotalDiskReadBytes();
			totalDiskWriteBytes = diskInfo.getTotalDiskWriteBytes();
			
			try {
				diskInfo.resetStats();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			diskInfo.processCommand(new String[0]);
			totalDiskReads = diskInfo.getTotalDiskReads() - totalDiskReads;
			totalDiskWrites = diskInfo.getTotalDiskWrites() - totalDiskWrites;
			totalDiskReadBytes = diskInfo.getTotalDiskReadBytes() - totalDiskReadBytes;
			totalDiskWriteBytes = diskInfo.getTotalDiskWriteBytes() - totalDiskWriteBytes;

//			logger.info("Reads: "+getTotalDiskReads()+"-"+getTotalDiskWrites());
//			logger.info("Bytes: "+getTotalDiskReadBytes()+"-"+getTotalDiskWriteBytes());
		} catch (ShellCommandUsageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandExecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initVersionInfo()
	{
		try {
			versionInfo.processCommand(new String[0]);
//			logger.info(versionInfo.getStatsCurrentFqdn());
//			logger.info(versionInfo.getStatsOSName());
//			logger.info(versionInfo.getStatsJavaVersion());
//			logger.info(versionInfo.getStatsJavaHome());
		} catch (ShellCommandUsageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandExecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initDiskSpaceInfo()
	{
		try {
			diskSpaceInfo.processCommand(new String[0]);
			//for testing
//			ArrayList<SingleDiskSpaceStatsHolder> listDiskStats = diskSpaceInfo.getListDiskStats();
//			for(int i=0;i<listDiskStats.size();i++) {
//				listDiskStats.get(i).printStats();
//			}
		} catch (ShellCommandUsageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandExecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getTotalDiskReads() {
		return Sigar.formatSize(totalDiskReads).trim();
	}

	public String getTotalDiskWrites() {
		return Sigar.formatSize(totalDiskWrites).trim();
	}
	
	public String getTotalDiskReadBytes() {
		//return Sigar.formatSize(totalDiskReadBytes).trim();
		
		//return raw in bytes
		return ""+totalDiskReadBytes;
	}

	public String getTotalDiskWriteBytes() {
		//return Sigar.formatSize(totalDiskWriteBytes).trim();
		
		//return raw in bytes
		return ""+totalDiskWriteBytes;
	}

	public StatsGeneric getCpuInfo() {
		return cpuInfo;
	}

	public StatsUpTime getUptimeInfo() {
		return uptimeInfo;
	}

	public StatsVersion getVersionInfo() {
		return versionInfo;
	}

	public static void main(String[] args) {
		new StatsManager();
	}

	public StatsDisksIO getDiskInfo() {
		return diskInfo;
	}

	public void setDiskInfo(StatsDisksIO diskInfo) {
		this.diskInfo = diskInfo;
	}

	public StatsDiskSpace getDiskSpaceInfo() {
		return diskSpaceInfo;
	}

	public void setDiskSpaceInfo(StatsDiskSpace diskSpaceInfo) {
		this.diskSpaceInfo = diskSpaceInfo;
	}

	public void setCpuInfo(StatsGeneric cpuInfo) {
		this.cpuInfo = cpuInfo;
	}

	public void setUptimeInfo(StatsUpTime uptimeInfo) {
		this.uptimeInfo = uptimeInfo;
	}

	public void setVersionInfo(StatsVersion versionInfo) {
		this.versionInfo = versionInfo;
	}

	public void setTotalDiskReads(long totalDiskReads) {
		this.totalDiskReads = totalDiskReads;
	}

	public void setTotalDiskWrites(long totalDiskWrites) {
		this.totalDiskWrites = totalDiskWrites;
	}

	public void setTotalDiskReadBytes(long totalDiskReadBytes) {
		this.totalDiskReadBytes = totalDiskReadBytes;
	}

	public void setTotalDiskWriteBytes(long totalDiskWriteBytes) {
		this.totalDiskWriteBytes = totalDiskWriteBytes;
	}

	
}