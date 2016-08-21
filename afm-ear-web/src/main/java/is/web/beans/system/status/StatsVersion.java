package is.web.beans.system.status;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;

import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;
import org.hyperic.sigar.win32.LocaleInfo;

/**
 * Display Sigar, java and system version information.
 */
public class StatsVersion extends SigarCommandBase {

	private String statsCurrentFqdn;
	private String statsCurrentUser;
	private String statsOSDescription;
	private String statsOSName;
	private String statsOSArch;
	private String statsOSMachine;
	private String statsOSVersion;
	private String statsOSPatchLevel;
	private String statsOSVendor;
	private String statsOSVendorVersion;
	private String statsOSCodeName;
	private String statsJavaVersion;
	private String statsJavaVendor;
	private String statsJavaHome;
	
    public StatsVersion(Shell shell) {
        super(shell);
    }

    public StatsVersion() {
        super();
    }

    public String getUsageShort() {
        return "Display sigar and system version info";
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    private void printNativeInfo(PrintStream os) {
        String version =
            "java=" + Sigar.VERSION_STRING +
            ", native=" + Sigar.NATIVE_VERSION_STRING;
        String build =
            "java=" + Sigar.BUILD_DATE +
            ", native=" + Sigar.NATIVE_BUILD_DATE;
        String scm =
            "java=" + Sigar.SCM_REVISION +
            ", native=" + Sigar.NATIVE_SCM_REVISION;
        String archlib =
            SigarLoader.getNativeLibraryName();

//        os.println("Sigar version......." + version);
//        os.println("Build date.........." + build);
//        os.println("SCM rev............." + scm);
        String host = getHostName();
        String fqdn;
        Sigar sigar = new Sigar(); 
        try {
            File lib = sigar.getNativeLibrary();
            if (lib != null) {
                archlib = lib.getName();
            }
            fqdn = sigar.getFQDN();
        } catch (SigarException e) {
            fqdn = "unknown";
        } finally {
            sigar.close();
        }

//        os.println("Archlib............." + archlib);
        statsCurrentFqdn = fqdn;
//        os.println("Current fqdn........" + fqdn);
        if (!fqdn.equals(host)) {
//          os.println("Hostname............" + host);
        }        

        if (SigarLoader.IS_WIN32) {
            LocaleInfo info = new LocaleInfo();
//            os.println("Language............" + info);
//            os.println("Perflib lang id....." + info.getPerflibLangId());
        }
    }
    
    public void printInfo(PrintStream os) {
        try {
            printNativeInfo(os);
        } catch (UnsatisfiedLinkError e) {
//            os.println("*******ERROR******* " + e);
        }

        statsCurrentUser = System.getProperty("user.name");
        OperatingSystem sys = OperatingSystem.getInstance();
        statsOSDescription = sys.getDescription();
        statsOSName = sys.getName();
        statsOSArch = sys.getArch();
        statsOSMachine = sys.getMachine();
        statsOSVersion = sys.getVersion();
        statsOSPatchLevel = sys.getPatchLevel();
        statsOSVendor = sys.getVendor();
        statsOSVendorVersion = sys.getVendorVersion();
        if (sys.getVendorCodeName() != null) {
            statsOSCodeName = sys.getVendorCodeName();
        }
        
        statsJavaVersion = System.getProperty("java.vm.version");
        statsJavaVendor = System.getProperty("java.vm.vendor");
        statsJavaHome = System.getProperty("java.home");
    }

    public void output(String[] args) {
        printInfo(this.out);
    }

    public static void main(String[] args) throws Exception {
        new StatsVersion().processCommand(args);
    }

	public String getStatsCurrentFqdn() {
		return statsCurrentFqdn;
	}

	public void setStatsCurrentFqdn(String statsCurrentFqdn) {
		this.statsCurrentFqdn = statsCurrentFqdn;
	}

	public String getStatsCurrentUser() {
		return statsCurrentUser;
	}

	public void setStatsCurrentUser(String statsCurrentUser) {
		this.statsCurrentUser = statsCurrentUser;
	}

	public String getStatsOSDescription() {
		return statsOSDescription;
	}

	public void setStatsOSDescription(String statsOSDescription) {
		this.statsOSDescription = statsOSDescription;
	}

	public String getStatsOSName() {
		return statsOSName;
	}

	public void setStatsOSName(String statsOSName) {
		this.statsOSName = statsOSName;
	}

	public String getStatsOSArch() {
		return statsOSArch;
	}

	public void setStatsOSArch(String statsOSArch) {
		this.statsOSArch = statsOSArch;
	}

	public String getStatsOSMachine() {
		return statsOSMachine;
	}

	public void setStatsOSMachine(String statsOSMachine) {
		this.statsOSMachine = statsOSMachine;
	}

	public String getStatsOSVersion() {
		return statsOSVersion;
	}

	public void setStatsOSVersion(String statsOSVersion) {
		this.statsOSVersion = statsOSVersion;
	}

	public String getStatsOSPatchLevel() {
		return statsOSPatchLevel;
	}

	public void setStatsOSPatchLevel(String statsOSPatchLevel) {
		this.statsOSPatchLevel = statsOSPatchLevel;
	}

	public String getStatsOSVendor() {
		return statsOSVendor;
	}

	public void setStatsOSVendor(String statsOSVendor) {
		this.statsOSVendor = statsOSVendor;
	}

	public String getStatsOSVendorVersion() {
		return statsOSVendorVersion;
	}

	public void setStatsOSVendorVersion(String statsOSVendorVersion) {
		this.statsOSVendorVersion = statsOSVendorVersion;
	}

	public String getStatsOSCodeName() {
		return statsOSCodeName;
	}

	public void setStatsOSCodeName(String statsOSCodeName) {
		this.statsOSCodeName = statsOSCodeName;
	}

	public String getStatsJavaVersion() {
		return statsJavaVersion;
	}

	public void setStatsJavaVersion(String statsJavaVersion) {
		this.statsJavaVersion = statsJavaVersion;
	}

	public String getStatsJavaVendor() {
		return statsJavaVendor;
	}

	public void setStatsJavaVendor(String statsJavaVendor) {
		this.statsJavaVendor = statsJavaVendor;
	}

	public String getStatsJavaHome() {
		return statsJavaHome;
	}

	public void setStatsJavaHome(String statsJavaHome) {
		this.statsJavaHome = statsJavaHome;
	}
    
    
}
