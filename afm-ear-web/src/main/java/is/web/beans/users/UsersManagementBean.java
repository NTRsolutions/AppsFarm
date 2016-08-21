package is.web.beans.users;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.business.UserRoles;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.security.SecurityManager;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAODeviceProfile;
import is.ejb.dl.dao.DAOProperty;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORole;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.DeviceProfileEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RoleEntity;
import is.ejb.dl.entities.UserEntity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

@ManagedBean(name="usersManagementBean")
@SessionScoped
public class UsersManagementBean implements Serializable {

	@Inject
	private Logger logger;

	@Inject
	private SecurityManager secManager;

	@Inject
	private DAOAdProvider daoAdProvider;
	@Inject
	private DAOProperty daoProperty;

	@Inject
	private SerDeMinimobProviderConfiguration serDeMocean;
	@Inject
	private SerDeFyberProviderConfiguration serDeFyber;
	@Inject
	private SerDeHasoffersProviderConfiguration serDeHasoffers;

	private UsersDataModelBean domainDataModel;
	private List<UserEntity> listUsers = new ArrayList<UserEntity>();

	private String userFormDetails = "Please fill in details";
	
	@Inject
	private DAOUser daoUser;

	@Inject
	private DAORole daoRole;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOAdProvider daoDomain;

	@Inject
	private DAODeviceProfile daoDeviceProfile;
	
	private UserEntity editedUser = new UserEntity();
	private UserEntity createdUser = new UserEntity();
	private LoginBean loginBean = null;

	private RealmEntity createdRealm = new RealmEntity();
	
	private String passwordOld = "";
	private String passwordNew1 = "";
	private String passwordNew2 = "";

	private String passwordCreate1 = "";
	private String passwordCreate2 = "";

	private String editedUserRoleName = "";
	private String createdUserRoleName = "";

	private String editedUserRealmName = "";
	private String createdUserRealmName = "";

	private ArrayList<RoleEntity> listCreatedUserRoles = new ArrayList<RoleEntity>();
	private ArrayList<RealmEntity> listRealms = new ArrayList<RealmEntity>();
	
	String loggedUserRoleName = "";
	
	//access rights based on user role
	private boolean disableButtonAddNewUser = true;
	private boolean disableButtonDeleteUser = true;
	private boolean renderRealmSetup = true;
	
	public UsersManagementBean() {
	}
	
   @PostConstruct
   public void init() {
	   //retrieve reference of an objection from session
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   
	   //extract logged user role name
	   List<RoleEntity> loggedUserRoles = (List<RoleEntity>) loginBean.getUser().getRoles();
	   loggedUserRoleName = loggedUserRoles.get(0).getName();
	   
	   initDefaultUsers();
	   
	   //regenerate list
	   refresh();
   }

	public void refresh() {
		try {
			//set default created user realm name
			createdUserRealmName = loginBean.getUser().getRealm().getName();
			
			logger.info("loading users...");
			listUsers = new ArrayList<UserEntity>();
			if(loginBean.getUser().hasRole(UserRoles.SUPER_USER)) { //get all users of role ADMIN and USER belonging to the realm
				listUsers = daoUser.findAll();	
			} else if(loginBean.getUser().hasRole(UserRoles.ADMIN)) { //get all users of role USER belonging to the realm
				listUsers = daoUser.findAll(loginBean.getUser().getRealm().getId());
				for(int i = listUsers.size()-1;i>=0; i--) {
					logger.info("user: "+listUsers.get(i).getName());
					if(listUsers.get(i).hasRole(UserRoles.SUPER_USER)) {
						listUsers.remove(listUsers.get(i));
					} 
//					else if(listUsers.get(i).hasRole(UserRoles.ADMIN) && !listUsers.get(i).getName().equals(loginBean.getUser().getName())) {
//						listUsers.remove(listUsers.get(i));
//					}
				}
			} else if(loginBean.getUser().hasRole(UserRoles.USER)) { //get only logged user
				listUsers.add(loginBean.getUser());	
			}
			
			//fill model with loaded list of scripts from all nodes
			domainDataModel = new UsersDataModelBean(listUsers);

			//configure GUI access rights
			if(! (loginBean.getUser().hasRole(UserRoles.ADMIN) || loginBean.getUser().hasRole(UserRoles.SUPER_USER))) {
				disableButtonAddNewUser = true;
			} else {
				disableButtonAddNewUser = false;
			}

			//fill realms list 
			try {
				listRealms = (ArrayList<RealmEntity>) daoRealm.findAll();
			} catch(Exception exc) {
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to process form: "+exc.toString()));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			}

//		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Users list sucessfully refreshed."));
//			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			RequestContext.getCurrentInstance().update("tabView:idUsersTable");
		} catch (Exception e) {
			listUsers = new ArrayList<UserEntity>();
			domainDataModel = new UsersDataModelBean(listUsers);

		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Error when retrieving users: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");

			e.printStackTrace();
			logger.severe("Error refreshing list of users: "+e.toString());
		}
	}

	public void initDefaultUsers() {
		//create default realm object for creating realms
		createdRealm = new RealmEntity();
		createdRealm.setName(userFormDetails);
		createdRealm.setDescription(userFormDetails);
		
		//authorise realm creation
		if(loggedUserRoleName.equals(UserRoles.SUPER_USER.toString())){
			renderRealmSetup = true;
		} else {
			renderRealmSetup = false;
		}
		//fill roles list according to user role
		try {
			listCreatedUserRoles = (ArrayList<RoleEntity>) daoRole.findAll();
			if(loggedUserRoleName.equals(UserRoles.ADMIN.toString())) { //if admin - remove su role to prevent admin create super users
				for(int i=0;i<listCreatedUserRoles.size();i++) {
					if(listCreatedUserRoles.get(i).getName().equals(UserRoles.SUPER_USER.toString())) {
						listCreatedUserRoles.remove(i);
					}
				}
			}
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to process form: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}

		createdUser = new UserEntity();
		createdUser.setName(userFormDetails);
		createdUser.setEmail(userFormDetails);
		createdUser.setLogin(userFormDetails);
		createdUser.setPassword(userFormDetails);
		//assign roles and realm
		try {
			RoleEntity role = daoRole.findByName(UserRoles.USER.toString());
			RealmEntity realm = daoRealm.findByName(loginBean.getUser().getRealm().getName());
			ArrayList<RoleEntity> roles = new ArrayList<RoleEntity>();
			roles.add(role);
			createdUser.setRealm(realm);
			createdUser.setRoles(roles);
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to process form: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}

		editedUser = new UserEntity();
		editedUser.setName("User name");
		editedUser.setEmail("User e-mail");
		editedUser.setLogin("Login");
		editedUser.setPassword("Password");
		//assign roles and realm
		try {
			RoleEntity role = daoRole.findByName(UserRoles.USER.toString());
			RealmEntity realm = daoRealm.findByName(loginBean.getUser().getRealm().getName());
			ArrayList<RoleEntity> roles = new ArrayList<RoleEntity>();
			roles.add(role);
			editedUser.setRealm(realm);
			editedUser.setRoles(roles);
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to process form: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}
	}
	
	public void saveUser(){
		logger.info("saving user: "+createdUser.getName()+" "+createdUser.getLogin()+" "+passwordCreate1+" "+createdUser.getEmail());
		try {

			UserEntity foundUser = daoUser.findByLogin(createdUser.getLogin());
			//verify if user login is already used
			if(createdUser.getLogin() == null || createdUser.getLogin().equals(userFormDetails) ||
					createdUser.getName() == null || createdUser.getName().equals(userFormDetails) ||
							passwordCreate1 == null || passwordCreate1.equals(userFormDetails) ||
					createdUser.getEmail() == null || createdUser.getEmail().equals(userFormDetails) )
			{
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Missing data! Please make sure that login, user name, email and password are provided"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			//} else if(foundUser != null && foundUser.getLogin().equals(createdUser.getLogin())) {				
			} else if(foundUser != null) {
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "User with login: "+createdUser.getLogin()+" already exists. Please provide different login name"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			} else if(passwordCreate1.length() == 0 || passwordCreate2.length() == 0 ||
						!passwordCreate1.equals(passwordCreate2)){ //check if passwords match
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Passwords empty or do not match!"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			} else { //create user
				//assign roles and realm
				RoleEntity role = daoRole.findByName(createdUserRoleName);
				RealmEntity realm = daoRealm.findByName(createdUserRealmName);
				ArrayList<RoleEntity> roles = new ArrayList<RoleEntity>();
				roles.add(role);
				
				createdUser.setRealm(realm);
				createdUser.setRoles(roles);
				createdUser.setPassword(secManager.generateStrongPasswordHash(passwordCreate1));
				
				daoUser.createOrUpdate(createdUser);
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "User successfully created."));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
				
				refresh();
			}
			
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to create new user: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}
	}

	public void updateUser(){
		logger.info("updating user: "+editedUser.getName());
		try {
			//assign roles and realm
			RoleEntity role = daoRole.findByName(editedUserRoleName);
			ArrayList<RoleEntity> roles = new ArrayList<RoleEntity>();
			roles.add(role);
			editedUser.setRoles(roles);
			if(!editedUser.getRealm().getName().equals(editedUserRealmName)) {
				logger.info("updating realm...");
				RealmEntity realm = daoRealm.findByName(editedUserRealmName);
				editedUser.setRealm(realm);
			}
			
			editedUser = daoUser.createOrUpdate(editedUser);
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "User settings successfully saved."));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			
			refresh();
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to save user settings: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}
	}

	public void deleteUser(){
		logger.info("deleting user: "+editedUser.getName());
		try {
//			ArrayList<RoleEntity> listRoles = new ArrayList<RoleEntity>();
//			editedUser.setRoles(listRoles);
//			daoUser.createOrUpdate(editedUser);//need to first update with 0 roles as otherwise we get foreighn key constraint error
			daoUser.delete(editedUser);
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "User successfully deleted."));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			
			refresh();
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to delete user: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			exc.printStackTrace();
		}
	}
	
	public void updatePassword(){
		logger.info("updating password: "+passwordOld+" "+passwordNew1+" "+passwordNew2);
		try {
			if(passwordOld != null && passwordOld.length() > 0) {

				//daoUser.findByNameAndCredentials(editedUser.getName(), editedUser.getLogin(), editedUser.getPassword());
				if(secManager.validatePassword(passwordOld, editedUser.getPassword())) {
					logger.info("old pass validated for user: "+editedUser.getName());
					if(passwordNew1 == null || passwordNew2 == null 
							|| passwordNew1.length() == 0 || passwordNew2.length() == 0 
							|| !passwordNew1.equals(passwordNew2)) {
						
					    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "New passwords do not match!"));
						RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
					} else {
						logger.info("updated password for user: "+editedUser.getName());
						editedUser.setPassword(secManager.generateStrongPasswordHash(passwordNew1));
						daoUser.createOrUpdate(editedUser);
						//resetPasswordFields();
					    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Password successfully updated."));
						RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
					}
				} else {
				    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Incorrect current password!"));
					RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
				}
			} else {
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Please provide current password!"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			}
			
		} catch(Exception exc) {
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to update password: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
		}
	}
	
	public void editUser(UserEntity user) {
		logger.info("editing user: "+user.getName());
		resetPasswordFields();
		editedUser = user;
		List<RoleEntity> userRoles = (List<RoleEntity>) user.getRoles();
		editedUserRoleName = userRoles.get(0).getName();
		editedUserRealmName = user.getRealm().getName();
		//buttons enable/disable
		if(loginBean.getUser().getLogin().equals(user.getLogin())){
			disableButtonDeleteUser = true;
		} else {
			disableButtonDeleteUser = false;
		}
	}
	
	public void resetPasswordFields() {
		passwordNew1 = "";
		passwordNew2 = "";
		passwordOld = "";
		RequestContext.getCurrentInstance().update("tabView:idChangePasswordData");
	}
	
	public void saveRealm(){
		logger.info("saving realm: "+createdRealm.getName()+" "+createdRealm.getDescription());
		try {

			RealmEntity foundRealm = daoRealm.findByName(createdRealm.getName());
			//verify if realm name is already used
			if(createdRealm.getName() == null || createdRealm.getName().length() == 0 || createdRealm.getName().equals(userFormDetails))
			{
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Missing data! Please make sure that realm name is provided"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			} else if(foundRealm != null) {
			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error", "Realm with name: "+createdRealm.getName()+" already exists. Please provide different realm name"));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
			} else { //create user
				//create realm
                createdRealm = daoRealm.createOrUpdate(createdRealm);

                /*
                //add domains
                Vector domains = new Vector();
                //domain 1
                AdProviderEntity domain = new AdProviderEntity();
                domain.setName("Default");
                domain.setRealm(createdRealm);
                domain = daoDomain.createOrUpdate(domain);
                domains.add(domain);
                
                domain = new AdProviderEntity();
                domain.setName("Test");
                domain.setRealm(createdRealm);
                domain = daoDomain.createOrUpdate(domain);
                domains.add(domain);
                
                //persist changes
                createdRealm.setDomains(domains);
                */
                
                //add domains
                Vector adProviders = new Vector();
                //domain 1
                AdProviderEntity adProvider = new AdProviderEntity();
                adProvider.setName("Mocean");
                adProvider.setRealm(createdRealm);
                adProvider.setActive(false);
                adProvider.setCodeName(OfferProviderCodeNames.MOCEAN.toString());
                adProvider.setTags("Mocean");
                MinimobProviderConfig moceanConfig = new MinimobProviderConfig();
                String strMoceanConfig = serDeMocean.serialize(moceanConfig);
                adProvider.setConfiguration(strMoceanConfig);
                adProvider = daoAdProvider.createOrUpdate(adProvider);
                adProviders.add(adProvider);

                //domain 2
                adProvider = new AdProviderEntity();
                adProvider.setName("Fyber");
                adProvider.setRealm(createdRealm);
                adProvider.setActive(false);
                adProvider.setCodeName(OfferProviderCodeNames.FYBER.toString());
                adProvider.setTags("Fyber");
                FyberProviderConfig fyberConfig = new FyberProviderConfig();
                String strFyberConfig = serDeFyber.serialize(fyberConfig);
                adProvider.setConfiguration(strFyberConfig);
                adProvider = daoAdProvider.createOrUpdate(adProvider);
                adProviders.add(adProvider);

                //domain 3
                adProvider = new AdProviderEntity();
                adProvider.setName("Hasoffers");
                adProvider.setRealm(createdRealm);
                adProvider.setActive(false);
                adProvider.setCodeName(OfferProviderCodeNames.HASOFFERS.toString());
                adProvider.setTags("Hasoffers");
                HasoffersProviderConfig hasoffersConfig = new HasoffersProviderConfig();
                String strHasoffersConfig = serDeHasoffers.serialize(hasoffersConfig);
                adProvider.setConfiguration(strHasoffersConfig);

                adProvider = daoAdProvider.createOrUpdate(adProvider);
                adProviders.add(adProvider);

                //createdRealm.setDomains(adProviders);
            	logger.info("Assigned default ad providers domains to Default realm...");
                createdRealm = daoRealm.createOrUpdate(createdRealm);

				Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, createdRealm.getId(), 
						LogStatus.OK, 
						"REALM_GENERATION created new realm: "+createdRealm.getName()+" id: "+createdRealm.getId());

			    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Realm successfully created."));
				RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");
				
				refresh();
			}
			
		} catch(Exception exc) {
			exc.printStackTrace();
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", "Unable to create new realm: "+exc.toString()));
			RequestContext.getCurrentInstance().update("tabView:idManageUsersGrowl");

			Application.getElasticSearchLogger().indexLog(Application.GENERIC_USER_ACTIVITY_LOG, createdRealm.getId(), 
					LogStatus.ERROR, 
					"REALM_GENERATION error creating new realm: "+createdRealm.getName()+" id: "+createdRealm.getId()+" exc: "+exc.toString());
		}
	}
	
	public void setDomainDataModel(UsersDataModelBean domainDataModel) {
		this.domainDataModel = domainDataModel;
	}

	public List<UserEntity> getListUsers() {
		return listUsers;
	}

	public void setListUsers(List<UserEntity> listUsers) {
		this.listUsers = listUsers;
	}

	public UserEntity getEditedUser() {
		return editedUser;
	}

	public void setEditedUser(UserEntity editedUser) {
		this.editedUser = editedUser;
	}

	public String getPasswordOld() {
		return passwordOld;
	}

	public void setPasswordOld(String passwordOld) {
		this.passwordOld = passwordOld;
	}

	public String getPasswordNew1() {
		return passwordNew1;
	}

	public void setPasswordNew1(String passwordNew1) {
		this.passwordNew1 = passwordNew1;
	}

	public String getPasswordNew2() {
		return passwordNew2;
	}

	public void setPasswordNew2(String passwordNew2) {
		this.passwordNew2 = passwordNew2;
	}

	public boolean isDisableButtonAddNewUser() {
		return disableButtonAddNewUser;
	}

	public void setDisableButtonAddNewUser(boolean disableButtonAddNewUser) {
		this.disableButtonAddNewUser = disableButtonAddNewUser;
	}

	public UserEntity getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(UserEntity createdUser) {
		this.createdUser = createdUser;
	}

	public String getPasswordCreate1() {
		return passwordCreate1;
	}

	public void setPasswordCreate1(String passwordCreate1) {
		this.passwordCreate1 = passwordCreate1;
	}

	public String getPasswordCreate2() {
		return passwordCreate2;
	}

	public void setPasswordCreate2(String passwordCreate2) {
		this.passwordCreate2 = passwordCreate2;
	}

	public boolean isDisableButtonDeleteUser() {
		return disableButtonDeleteUser;
	}

	public void setDisableButtonDeleteUser(boolean disableButtonDeleteUser) {
		this.disableButtonDeleteUser = disableButtonDeleteUser;
	}

	public String getEditedUserRoleName() {
		return editedUserRoleName;
	}

	public void setEditedUserRoleName(String editedUserRoleName) {
		this.editedUserRoleName = editedUserRoleName;
	}

	public String getCreatedUserRoleName() {
		return createdUserRoleName;
	}

	public void setCreatedUserRoleName(String createdUserRoleName) {
		this.createdUserRoleName = createdUserRoleName;
	}

	public ArrayList<RoleEntity> getListCreatedUserRoles() {
		return listCreatedUserRoles;
	}

	public void setListCreatedUserRoles(ArrayList<RoleEntity> listCreatedUserRoles) {
		this.listCreatedUserRoles = listCreatedUserRoles;
	}

	public ArrayList<RealmEntity> getListRealms() {
		return listRealms;
	}

	public void setListRealms(ArrayList<RealmEntity> listRealms) {
		this.listRealms = listRealms;
	}

	public boolean isRenderRealmSetup() {
		return renderRealmSetup;
	}

	public void setRenderRealmSetup(boolean renderRealmSetup) {
		this.renderRealmSetup = renderRealmSetup;
	}

	public String getEditedUserRealmName() {
		return editedUserRealmName;
	}

	public void setEditedUserRealmName(String editedUserRealmName) {
		this.editedUserRealmName = editedUserRealmName;
	}

	public String getCreatedUserRealmName() {
		return createdUserRealmName;
	}

	public void setCreatedUserRealmName(String createdUserRealmName) {
		this.createdUserRealmName = createdUserRealmName;
	}

	public RealmEntity getCreatedRealm() {
		return createdRealm;
	}

	public void setCreatedRealm(RealmEntity createdRealm) {
		this.createdRealm = createdRealm;
	}

	
	
}

