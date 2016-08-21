package is.ejb.dl.entities;

import is.ejb.dl.entities.PropertyEntityPK;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@XmlRootElement
@Table(name = "Property")
public class PropertyEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   public static final int TYPE_APPLICATION=1;
   public static final int TYPE_PROFILE=2;
   public static final int TYPE_CPE=3;

   PropertyEntityPK pk;
   
   private String value;

    public PropertyEntity() {}

	public PropertyEntity(Integer parentId, Integer type, String name, String value)
	   
	{
		//mzj init?
		pk = new PropertyEntityPK();
		
		setParentId(parentId);
		setType(type);
        setName(name);
        setValue(value);
	}

	@EmbeddedId
	public PropertyEntityPK getPk()
	{
		return pk;
	}
   
	public void setPk(PropertyEntityPK pk)
	{
		this.pk = pk;
	}

	public void setParentId(Integer parentId) {
	  	pk.setParentId(parentId);
	}

    public void setName(String name)
    {
    	pk.setName(name);
    }

	public void setType(Integer type) {
	  	pk.setType(type);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
