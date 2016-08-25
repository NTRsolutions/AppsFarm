package is.ejb.dl.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "Countries")
public class CountryEntity {

	@Id
	@GeneratedValue
	private int id;

	private Timestamp generationTime;
	private String code;
	private String name;

	@ManyToOne
	@JoinColumn(name = "realm", referencedColumnName = "id")
	private RealmEntity realm;

	
	public int getId() {
		return id;
	}

	public Timestamp getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(Timestamp generationTime) {
		this.generationTime = generationTime;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}
	
}
