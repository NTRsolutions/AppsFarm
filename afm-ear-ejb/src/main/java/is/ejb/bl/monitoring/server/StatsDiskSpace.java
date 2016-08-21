package is.ejb.bl.monitoring.server;


import java.util.ArrayList;
import java.util.logging.Logger;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NfsFileSystem;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.shell.FileCompleter;
import org.hyperic.sigar.util.GetlineCompleter;

/**
 * Report filesytem disk space usage.
 */
public class StatsDiskSpace extends StatsCommandBase {

	private static Logger logger = Logger.getLogger("StatsDiskSpace");

	private ArrayList<SingleDiskSpaceStatsHolder> listDiskStats = new ArrayList<SingleDiskSpaceStatsHolder>();
	
    private static final String OUTPUT_FORMAT =
        "%-15s %4s %4s %5s %4s %-15s %s";

    //like df -h -a
    private static final String[] HEADER = new String[] {
        "Filesystem",
        "Size",
        "Used",
        "Avail",
        "Use%",
        "Mounted on",
        "Type"
    };
    //df -i
    private static final String[] IHEADER = new String[] {
        "Filesystem",
        "Inodes",
        "IUsed",
        "IFree",
        "IUse%",
        "Mounted on",
        "Type"
    };

    private GetlineCompleter completer;
    private boolean opt_i;

    public StatsDiskSpace(Shell shell) {
        super(shell);
        setOutputFormat(OUTPUT_FORMAT);
        this.completer = new FileCompleter(shell);
    }

    public StatsDiskSpace() {
        super();
        setOutputFormat(OUTPUT_FORMAT);
    }

    public GetlineCompleter getCompleter() {
        return this.completer;
    }

    protected boolean validateArgs(String[] args) {
        return true;
    }

    public String getSyntaxArgs() {
        return "[filesystem]";
    }

    public String getUsageShort() {
        return "Report filesystem disk space usage";
    }

    public void printHeader() {
        //printf(this.opt_i ? IHEADER : HEADER);
    }

    public void output(String[] args) throws SigarException {
        this.opt_i = false;
        ArrayList sys = new ArrayList();

        if (args.length > 0) {
            FileSystemMap mounts = this.proxy.getFileSystemMap();
            for (int i=0; i<args.length; i++) {
                String arg = args[i];
                if (arg.equals("-i")) {
                    this.opt_i = true;
                    continue;
                }
                String name = FileCompleter.expand(arg);
                FileSystem fs = mounts.getMountPoint(name);

                if (fs == null) {
                    throw new SigarException(arg + " No such file or directory");
                }
                sys.add(fs);
            }
        }
        if (sys.size() == 0) {
            FileSystem[] fslist = this.proxy.getFileSystemList();
            for (int i=0; i<fslist.length; i++) {
                sys.add(fslist[i]);
            }
        }

        printHeader();
        for (int i=0; i<sys.size(); i++) {
            output((FileSystem)sys.get(i));
        }
    }

    public void output(FileSystem fs) throws SigarException {
        long used, avail, total, pct;

        try {
            FileSystemUsage usage;
            if (fs instanceof NfsFileSystem) {
                NfsFileSystem nfs = (NfsFileSystem)fs;
                if (!nfs.ping()) {
                    //println(nfs.getUnreachableMessage());
                    return;
                }
            }
            usage = this.sigar.getFileSystemUsage(fs.getDirName());
            if (this.opt_i) {
                used  = usage.getFiles() - usage.getFreeFiles();
                avail = usage.getFreeFiles();
                total = usage.getFiles();
                if (total == 0) {
                    pct = 0;
                }
                else {
                    long u100 = used * 100;
                    pct = u100 / total +
                        ((u100 % total != 0) ? 1 : 0);
                }
            }
            else {
                used = usage.getTotal() - usage.getFree();
                avail = usage.getAvail();
                total = usage.getTotal();

                pct = (long)(usage.getUsePercent() * 100);
            }
        } catch (SigarException e) {
            //e.g. on win32 D:\ fails with "Device not ready"
            //if there is no cd in the drive.
            used = avail = total = pct = 0;
        }

        String usePct;
        if (pct == 0) {
            usePct = "-";
        }
        else {
            usePct = pct + "%";
        }
        
        ArrayList items = new ArrayList();

        items.add(fs.getDevName());
        items.add(formatSize(total));
        items.add(formatSize(used));
        items.add(formatSize(avail));
        items.add(usePct);
        items.add(fs.getDirName());
        items.add(fs.getSysTypeName() + "/" + fs.getTypeName());
        
        //String appPath= NodeManager.getApplicationPath();
        //if use percentage string equals to '-' then the disk has no size and we do not add it to to stats!
        if(!usePct.equals("-")) {
            SingleDiskSpaceStatsHolder statsDiskSpace = new SingleDiskSpaceStatsHolder();
            statsDiskSpace.setFileSystemName(fs.getDevName());
            statsDiskSpace.setSizeTotal(formatSize(total));
            statsDiskSpace.setSizeTotalValue(total);
            statsDiskSpace.setSizeUsed(formatSize(used));
            statsDiskSpace.setSizeUsagePercentage(usePct);
            statsDiskSpace.setSizeAvailable(formatSize(avail));
            statsDiskSpace.setFileSystemMountPath(fs.getDirName());
            statsDiskSpace.setFileSystemType(fs.getSysTypeName() + "/" + fs.getTypeName());

        	//if(appPath.startsWith(statsDiskSpace.getFileSystemMountPath()) && statsDiskSpace.getFileSystemMountPath().length() > 1) {
                //logger.debug("app path: "+appPath+" identified as main drive: "+statsDiskSpace.getFileSystemMountPath());
                statsDiskSpace.setMainDrive(true);
        	//}

            //statsDiskSpace.printStats();
            listDiskStats.add(statsDiskSpace);
        }
        
        //printf(items);
    }

    private String formatSize(long size) {
        return this.opt_i ? String.valueOf(size) : Sigar.formatSize(size * 1024);
    }

    public static void main(String[] args) throws Exception {
        new StatsDiskSpace().processCommand(args);
    }

	public ArrayList<SingleDiskSpaceStatsHolder> getListDiskStats() {
		return listDiskStats;
	}

	public void setListDiskStats(ArrayList<SingleDiskSpaceStatsHolder> listDiskStats) {
		this.listDiskStats = listDiskStats;
	}

	public SingleDiskSpaceStatsHolder getNodeMainDisk() {
		//get identified main drive
		for(int i=0;i<listDiskStats.size();i++) {
			if(listDiskStats.get(i).isMainDrive()) {
				//logger.info("identified node main mount point as: "+listDiskStats.get(i).getFileSystemName()+" of size: "+listDiskStats.get(i).getSizeTotal());
				return listDiskStats.get(i);
			}
		}
		//if we haven't been able to identify main drive - return the largest mount point
		int largestMountPointIndex = 0;
		long largestMountSize = 0;
		
		//get identified main drive
		for(int i=0;i<listDiskStats.size();i++) {
			if(largestMountSize < listDiskStats.get(i).getSizeTotalValue()) {
				largestMountSize = listDiskStats.get(i).getSizeTotalValue();
				largestMountPointIndex = i;
			}
		}

		//logger.info("!unable to directly identify node main mount point - returning the largest in size mount point as the main disk: "+listDiskStats.get(largestMountPointIndex).getFileSystemName()+" of size: "+listDiskStats.get(largestMountPointIndex).getSizeTotal());

		return listDiskStats.get(largestMountPointIndex);
	}

}
