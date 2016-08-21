package is.web.beans.system.status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;

/**
 * Display cpu information for each cpu found on the system.
 */
public class StatsGeneric extends SigarCommandBase {

    public boolean displayTimes = true;

    static Map<String, Long> rxCurrentMap = new HashMap<String, Long>();
    static Map<String, List<Long>> rxChangeMap = new HashMap<String, List<Long>>();
    static Map<String, Long> txCurrentMap = new HashMap<String, Long>();
    static Map<String, List<Long>> txChangeMap = new HashMap<String, List<Long>>();

    //cpu
    private String statsCpuModelName;
    private int statsCpuModelSpeed;
    private int statsPhysicalCpus;
    private int statsNumberOfCores;
    private long statsCpuCacheSize;
    private String statsCpuUtilisation;
    
    //mem
    private long statsMemTotal;
    private long statsMemUsed;
    private long statsMemFree;
    
    //network I/O
    private String statsNetworkDownloadSpeed;
    private String statsNetworkUploadSpeed;
    
    //network info
    private String statsNetworkPrimaryInterface;
    private String statsNetworkPrimaryIpAddress;
    private String statsNetworkPrimaryMacAddress;
    private String statsNetworkPrimaryNetmask;
    private String statsNetworkHostName;
    private String statsNetworkDomainName;
    private String statsNetworkDefaultGateway;
    private String statsNetworkPrimaryDns;
    private String statsNetworkSecondaryDns;
    
    public StatsGeneric(Shell shell) {
        super(shell);
    }

    public StatsGeneric() {
        super();
    }

    public String getUsageShort() {
        return "Display cpu information";
    }

    private void output(CpuPerc cpu) {
        if (SigarLoader.IS_LINUX) {
        }
        
        statsCpuUtilisation = CpuPerc.format(cpu.getCombined());
    }

    public void output(String[] args) throws SigarException {
    
    	//get CPU stats
        org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();
        CpuPerc[] cpus = this.sigar.getCpuPercList();

        org.hyperic.sigar.CpuInfo info = infos[0];
        long cacheSize = info.getCacheSize();
        
        statsCpuModelName = info.getModel();
        statsCpuModelSpeed = info.getMhz();
        statsNumberOfCores = info.getTotalCores();
        
        if ((info.getTotalCores() != info.getTotalSockets()) ||
            (info.getCoresPerSocket() > info.getTotalCores()))
        {
            statsPhysicalCpus = info.getTotalSockets();
            statsNumberOfCores = info.getCoresPerSocket();
        }

        if (cacheSize != Sigar.FIELD_NOTIMPL) {
            statsCpuCacheSize = cacheSize;
        }

        if (!this.displayTimes) {
            return;
        }

        for (int i=0; i<cpus.length; i++) {
            output(cpus[i]);
        }

        output(this.sigar.getCpuPerc());
        
        //get memory stats
        Mem mem   = this.sigar.getMem();
        Swap swap = this.sigar.getSwap();

        statsMemTotal = format(mem.getTotal());
        statsMemUsed = format(mem.getUsed());
        statsMemFree = format(mem.getFree());

        //get network I/O
        getMetric(this.sigar);
        //need to wait for network I/O stats to generate
        try {
        	Thread.sleep(1000);
        } catch(Exception exc) {}

        Long[] m = getMetric(this.sigar);
        long totalrx = m[0];
        long totaltx = m[1];
        //statsNetworkDownloadSpeed = Sigar.formatSize(totalrx);
        //statsNetworkUploadSpeed = Sigar.formatSize(totaltx);

        //send unformatted value in bytes
        statsNetworkDownloadSpeed = ""+totalrx;
        statsNetworkUploadSpeed = ""+totaltx;

        //get network info
        NetInterfaceConfig config = this.sigar.getNetInterfaceConfig(null);
        statsNetworkPrimaryInterface = config.getName();
        statsNetworkPrimaryIpAddress = config.getAddress();
        statsNetworkPrimaryMacAddress = config.getHwaddr();
        statsNetworkPrimaryNetmask = config.getNetmask();

        org.hyperic.sigar.NetInfo netInfo = this.sigar.getNetInfo();
        statsNetworkHostName = netInfo.getHostName();
        statsNetworkDomainName = netInfo.getDomainName();
        statsNetworkDefaultGateway = netInfo.getDefaultGateway();
        statsNetworkPrimaryDns = netInfo.getPrimaryDns();
        statsNetworkSecondaryDns = netInfo.getSecondaryDns();
    }
    
    public String getStatsCpuModelName() {
		return statsCpuModelName;
	}

	public void setStatsCpuModelName(String statsCpuModelName) {
		this.statsCpuModelName = statsCpuModelName;
	}

	public int getStatsCpuModelSpeed() {
		return statsCpuModelSpeed;
	}

	public int getStatsPhysicalCpus() {
		return statsPhysicalCpus;
	}

	public int getStatsNumberOfCores() {
		return statsNumberOfCores;
	}

	public long getStatsCpuCacheSize() {
		return statsCpuCacheSize;
	}

	public String getStatsCpuUtilisation() {
		String strCpuLoad = statsCpuUtilisation.trim();
		strCpuLoad = strCpuLoad.substring(0, strCpuLoad.length()-1);

		return strCpuLoad;
	}

	public static void main(String[] args) throws Exception {
        new StatsGeneric().processCommand(args);
    }
	
	
	
    public long getStatsMemTotal() {
		return statsMemTotal;
	}

	public long getStatsMemUsed() {
		return statsMemUsed;
	}

	public long getStatsMemFree() {
		return statsMemFree;
	}

	//ram format
	private static Long format(long value) {
        return new Long(value / 1024);
    }

	//network I/O helper functions
    public static String networkInfo(Sigar sigar) throws SigarException {
        String info = sigar.getNetInfo().toString();
        info += "\n "+ sigar.getNetInterfaceConfig().toString();
        return info;
    }

    public static String getDefaultGateway(Sigar sigar) throws SigarException {
        return sigar.getNetInfo().getDefaultGateway();
    }

    public static Long[] getMetric(Sigar sigar) throws SigarException {
        for (String ni : sigar.getNetInterfaceList()) {
            // System.out.println(ni);
            NetInterfaceStat netStat = sigar.getNetInterfaceStat(ni);
            NetInterfaceConfig ifConfig = sigar.getNetInterfaceConfig(ni);
            String hwaddr = null;
            if (!NetFlags.NULL_HWADDR.equals(ifConfig.getHwaddr())) {
                hwaddr = ifConfig.getHwaddr();
            }
            if (hwaddr != null) {
                long rxCurrenttmp = netStat.getRxBytes();
                saveChange(rxCurrentMap, rxChangeMap, hwaddr, rxCurrenttmp, ni);
                long txCurrenttmp = netStat.getTxBytes();
                saveChange(txCurrentMap, txChangeMap, hwaddr, txCurrenttmp, ni);
            }
        }
        long totalrx = getMetricData(rxChangeMap);
        long totaltx = getMetricData(txChangeMap);
        for (List<Long> l : rxChangeMap.values())
            l.clear();
        for (List<Long> l : txChangeMap.values())
            l.clear();
        return new Long[] { totalrx, totaltx };
    }

    private static long getMetricData(Map<String, List<Long>> rxChangeMap) {
        long total = 0;
        for (Entry<String, List<Long>> entry : rxChangeMap.entrySet()) {
            int average = 0;
            for (Long l : entry.getValue()) {
                average += l;
            }
            total += average / entry.getValue().size();
        }
        return total;
    }

    private static void saveChange(Map<String, Long> currentMap,
            Map<String, List<Long>> changeMap, String hwaddr, long current,
            String ni) {
        Long oldCurrent = currentMap.get(ni);
        if (oldCurrent != null) {
            List<Long> list = changeMap.get(hwaddr);
            if (list == null) {
                list = new LinkedList<Long>();
                changeMap.put(hwaddr, list);
            }
            list.add((current - oldCurrent));
        }
        currentMap.put(ni, current);
    }

	public String getStatsNetworkDownloadSpeed() {
		return statsNetworkDownloadSpeed;
	}

	public String getStatsNetworkUploadSpeed() {
		return statsNetworkUploadSpeed;
	}

	//network info
	public String getStatsNetworkPrimaryInterface() {
		return statsNetworkPrimaryInterface;
	}

	public String getStatsNetworkPrimaryIpAddress() {
		return statsNetworkPrimaryIpAddress;
	}

	public String getStatsNetworkPrimaryMacAddress() {
		return statsNetworkPrimaryMacAddress;
	}

	public String getStatsNetworkPrimaryNetmask() {
		return statsNetworkPrimaryNetmask;
	}

	public String getStatsNetworkHostName() {
		return statsNetworkHostName;
	}

	public String getStatsNetworkDomainName() {
		return statsNetworkDomainName;
	}

	public String getStatsNetworkDefaultGateway() {
		return statsNetworkDefaultGateway;
	}

	public String getStatsNetworkPrimaryDns() {
		return statsNetworkPrimaryDns;
	}

	public String getStatsNetworkSecondaryDns() {
		return statsNetworkSecondaryDns;
	}
	
    
}
