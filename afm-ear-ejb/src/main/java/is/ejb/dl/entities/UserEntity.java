package is.ejb.dl.entities;

import is.ejb.bl.business.UserRoles;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
@Table(name = "User")
public class UserEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   private int id;

   @NotNull
   @Size(min = 1, max = 25)
   //@Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
   private String name;

   @NotNull
   @NotEmpty
   //@Email
   private String email;

   @NotNull
   @NotEmpty
   @Column(unique=true)
   private String login;

   @NotNull
   @NotEmpty
   private String password;

   @ManyToOne
   @JoinColumn(name = "Realm", referencedColumnName = "id")
   private RealmEntity realm;

   @JoinTable(name="User_Role")
   @ManyToMany(fetch = FetchType.EAGER)
   private Collection<RoleEntity> roles;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
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

	public RealmEntity getRealm() {
		return realm;
	}

	public void setRealm(RealmEntity realm) {
		this.realm = realm;
	}

	public Collection<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Collection<RoleEntity> roles) {
		this.roles = roles;
	}

	public String getRolesString() {
		String rolesString = "";
		if(roles == null || roles.size() == 0) {
			rolesString = "No roles assigned yet";
		} else {
			Iterator i = roles.iterator();
			while(i.hasNext()) {
				RoleEntity role = (RoleEntity)i.next();
				rolesString = ""+role.getName()+"";
				//rolesString = "["+role.getName()+"] ";
			}
		}
		return rolesString;
	}

	public boolean hasRole(UserRoles roleName) {
		Iterator i = roles.iterator();
		while(i.hasNext()) {
			RoleEntity role = (RoleEntity)i.next();
			if(role.getName().equals(roleName.toString())) {
				return true;
			}
		}
		
		return false;
	}

	
}



