package is.ejb.dl.entities;

import javax.persistence.Embeddable;

@Embeddable
public class ProfilePropertyEntityPK implements java.io.Serializable{

	private String profilename;
	private String name;

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public ProfilePropertyEntityPK() {
    }

    public ProfilePropertyEntityPK(String profilename, String name) {
        this.profilename = profilename;
        this.name = name;
    }

    @Override
    public boolean equals(java.lang.Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof ProfilePropertyEntityPK)) {
            return false;
        }
        ProfilePropertyEntityPK other = (ProfilePropertyEntityPK) otherOb;
        return ((profilename == null ? other.profilename == null : profilename.equals(other.profilename)) &&
                (name == null ? other.name == null : name.equals(other.name)));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ((profilename == null ? 0 : profilename.hashCode()) ^
                (name == null ? 0 : name.hashCode()));
    }

	public String getProfilename() {
		return profilename;
	}

	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
