package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "PersonalDetails")
public class PersonalDetailsEntity {
	@Id
	@GeneratedValue
	private int id;
	private int userId;
	private String name;
	private String surname;
	private String houseNumber;
	private String street;
	private String postCode;
	private String country;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getHouseNumber() {
		return houseNumber;
	}
	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	@Override
	public String toString() {
		return "PersonalDetailsEntity [id=" + id + ", userId=" + userId + ", name=" + name + ", surname=" + surname
				+ ", houseNumber=" + houseNumber + ", street=" + street + ", postCode=" + postCode + ", country="
				+ country + ", getId()=" + getId() + ", getUserId()=" + getUserId() + ", getName()=" + getName()
				+ ", getSurname()=" + getSurname() + ", getHouseNumber()=" + getHouseNumber() + ", getStreet()="
				+ getStreet() + ", getPostCode()=" + getPostCode() + ", getCountry()=" + getCountry() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
	
	
}
