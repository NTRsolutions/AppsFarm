package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "RadiusConfiguration")
public class RadiusConfigurationEntity {
	@Id
	@GeneratedValue
	private int id;
	private String ip;
	private int port;
	private String login;
	private String password;
	private String dbname;
	private int cloudtraxId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	

	public int getCloudtraxId() {
		return cloudtraxId;
	}

	public void setCloudtraxId(int cloudtraxId) {
		this.cloudtraxId = cloudtraxId;
	}

	@Override
	public String toString() {
		return "RadiusConfigurationEntity [id=" + id + ", ip=" + ip + ", port="
				+ port + ", login=" + login + ", password=" + password
				+ ", dbname=" + dbname + ", cloudtraxId=" + cloudtraxId + "]";
	}

}
