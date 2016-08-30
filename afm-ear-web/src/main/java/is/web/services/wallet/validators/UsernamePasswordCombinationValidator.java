package is.web.services.wallet.validators;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.entities.AppUserEntity;
import is.web.services.APIValidator;

@ManagedBean
public class UsernamePasswordCombinationValidator implements APIValidator {

	@Inject
	private DAOAppUser daoAppUser;


	
	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			String username = (String) parameters.get("username");
			String password = (String) parameters.get("password");

			AppUserEntity appUser = daoAppUser.findByUsername(username);
			String passwordHash = getPasswordHash(password);
			if (appUser.getPassword().equals(passwordHash)){
				return true;
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}

	}

	public String getPasswordHash(String password) {
		String saltValue = "AppsfArm && S!S_salt stri!ng";
		return DigestUtils.sha1Hex(password + saltValue);
	}
	
	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_USERNAME_PASSWORD_COMBINATION;
	}

}
