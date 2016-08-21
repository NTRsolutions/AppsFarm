package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "AccessPoints")
public class AccessPointEntity {

	@Id
	@GeneratedValue
	private int id;
	private String mac;
	private int cloudtraxId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public int getCloudtraxId() {
		return cloudtraxId;
	}
	public void setCloudtraxId(int cloudtraxId) {
		this.cloudtraxId = cloudtraxId;
	}
	@Override
	public String toString() {
		return "AccessPointEntity [id=" + id + ", mac=" + mac
				+ ", cloudtraxId=" + cloudtraxId + "]";
	}
	
	
}
