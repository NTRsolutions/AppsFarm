package is.ejb.bl.monitoring.server;

import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarNotImplementedException;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;
import org.hyperic.sigar.util.PrintfFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Display how long the system has been running and the current load averages.
 */
public class StatsUpTime extends SigarCommandBase {

    private static Object[] loadAvg = new Object[3];

    private String statsCurrentTime;
    private String statsUpTime;
    private String statsLoadAverage;
    
    private static PrintfFormat formatter = new PrintfFormat("%.2f, %.2f, %.2f");

    public StatsUpTime(Shell shell) {
        super(shell);
    }

    public StatsUpTime() {
        super();
    }

    public String getUsageShort() {
        return "Display how long the system has been running";
    }

    public void output(String[] args) throws SigarException {
        getInfo(this.sigar);
    }

    public String getInfo(SigarProxy sigar) throws SigarException {
        double uptime = sigar.getUptime().getUptime();

        String loadAverage;

        try {
            double[] avg = sigar.getLoadAverage();
            loadAvg[0] = new Double(avg[0]);
            loadAvg[1] = new Double(avg[1]);
            loadAvg[2] = new Double(avg[2]);

            loadAverage = formatter.sprintf(loadAvg);
            
        } catch (SigarNotImplementedException e) {
            loadAverage = "(load average unknown)";
        }

        statsCurrentTime = getCurrentTime();
        statsLoadAverage = loadAverage;
        statsUpTime = formatUptime(uptime);
        
        return
            "  " + getCurrentTime() + 
            "  up " + formatUptime(uptime) +
            ", " + loadAverage;
    }

    private static String formatUptime(double uptime) {
        String retval = "";

        int days = (int)uptime / (60*60*24);
        int minutes, hours;

        if (days != 0) {
            retval += days + " " + ((days > 1) ? "days" : "day") + ", ";
        }

        minutes = (int)uptime / 60;
        hours = minutes / 60;
        hours %= 24;
        minutes %= 60;

        if (hours != 0) {
            retval += hours + ":" + minutes;
        }
        else {
            retval += minutes + " min";
        }

        return retval;
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("h:mm a").format(new Date());
    }

    //pretty close to uptime command, but we don't output number of users yet
    public static void main(String[] args) throws Exception {
        new StatsUpTime().processCommand(args);
    }

	public String getStatsCurrentTime() {
		return statsCurrentTime;
	}

	public void setStatsCurrentTime(String statsCurrentTime) {
		this.statsCurrentTime = statsCurrentTime;
	}

	public String getStatsUpTime() {
		return statsUpTime;
	}

	public void setStatsUpTime(String statsUpTime) {
		this.statsUpTime = statsUpTime;
	}

	public String getStatsLoadAverage() {
		return statsLoadAverage;
	}

	public void setStatsLoadAverage(String statsLoadAverage) {
		this.statsLoadAverage = statsLoadAverage;
	}
    
    
}
