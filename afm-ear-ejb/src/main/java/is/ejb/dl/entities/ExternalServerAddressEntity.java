package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "ExternalServerAddress")
public class ExternalServerAddressEntity {

	@Id
	@GeneratedValue
	private int id;

	@Lob
	private String ipContent;

	private String externalServerType;

	private boolean enabled;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExternalServerType() {
		return externalServerType;
	}

	public void setExternalServerType(String externalServerType) {
		this.externalServerType = externalServerType;
	}

	public String getIpContent() {
		return ipContent;
	}

	public void setIpContent(String ipContent) {
		this.ipContent = ipContent;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "ExternalServerAddressEntity [id=" + id + ", ipContent=" + ipContent + ", externalServerType="
				+ externalServerType + ", enabled=" + enabled + "]";
	}

	

}
