package is.ejb.bl.reporting;

import javax.ejb.Stateless;

@Stateless
public interface IEventLog {
	
	String toCSV();
	
}
