package is.ejb.dl.entities;

import javax.persistence.Embeddable;

@Embeddable
public class PropertyEntityPK implements java.io.Serializable{

    private Integer parentId;
    private Integer type;
    private String name;
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public PropertyEntityPK () {
    }
    public PropertyEntityPK (Integer parentId, Integer type, String name) {
        this.parentId = parentId;
        this.type = type;
        this.name = name;
    }
    
    @Override
    public boolean equals(java.lang.Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof PropertyEntityPK)) {
            return false;
        }
        PropertyEntityPK other = (PropertyEntityPK) otherOb;
        return (

                (parentId==null?other.parentId==null:parentId == other.parentId)
                &&
                (type==null?other.type==null:type==other.type)
                &&
                (name==null?other.name==null:name.equals(other.name))
                );
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (

                (parentId==null?0:parentId.hashCode())
                ^
                (type==null?0:type.hashCode())
                ^
                (name==null?0:name.hashCode())
                );
    }

    public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


}
