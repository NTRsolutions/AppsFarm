package is.ejb.dl.entities;

import is.ejb.bl.system.MaintenanceConfigurationHolder;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.executable.ValidateOnExecution;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
@Table(name = "MaintenanceConfiguration")
public class MaintenanceConfigurationEntity implements Serializable {
   /** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
    @Id
    @GeneratedValue
    private int maintenanceConfigurationId;

    @NotNull
    @Lob
    @Column(length=1048576) 
	private String content = "";
   
    @NotNull
	private Timestamp timestamp;

    @NotNull
    private boolean active;
    
    @Transient
    private MaintenanceConfigurationHolder configurationHolder = null;
    
    public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public MaintenanceConfigurationHolder getConfigurationHolder() {
		return configurationHolder;
	}

	public void setConfigurationHolder(
			MaintenanceConfigurationHolder configurationHolder) {
		this.configurationHolder = configurationHolder;
	}
	
}


