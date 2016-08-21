package is.ejb.dl.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;


@Entity
@XmlRootElement
@Table(name = "ProfileProperty")
public class ProfilePropertyEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   static final int TYPE_APPLICATION=1;
   static final int TYPE_PROFILE=2;
   static final int TYPE_CPE=3;
   
   ProfilePropertyEntityPK pk;
   
   private String value;

   @ManyToOne(fetch = FetchType.EAGER)
   private DeviceProfileEntity deviceProfile;

   @ManyToOne(fetch = FetchType.EAGER)
   public DeviceProfileEntity getDeviceProfile() {
		return deviceProfile;
   }

	public void setDeviceProfile(DeviceProfileEntity profile) {
		this.deviceProfile = profile;
	}

   public ProfilePropertyEntity() {}

	public ProfilePropertyEntity(String profile, String name, String value)
	   
	{
        setName(name);
        setValue(value);
	}

	@EmbeddedId
	public ProfilePropertyEntityPK getPk()
	{
		return pk;
	}
   
	public void setPk(ProfilePropertyEntityPK pk)
	{
		this.pk = pk;
	}

    public void setName(String name)
    {
    	pk.setProfilename(name);
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
