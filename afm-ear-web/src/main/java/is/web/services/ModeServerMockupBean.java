package is.web.services;

import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlElement;import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ModeServerMockupBean {
	@XmlElement public String MSISDN;
	@XmlElement public String OriginTransactionID;
	@XmlElement public String Reward;
	@XmlElement public String ISOCurrCode;
	@XmlElement public String User;
	@XmlElement public String Password;
	@XmlElement public String Operator;
	
	
}