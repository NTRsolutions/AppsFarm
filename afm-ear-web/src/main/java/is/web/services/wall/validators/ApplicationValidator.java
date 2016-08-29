package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.web.services.APIValidator;

@ManagedBean
public class ApplicationValidator implements APIValidator {

	@Inject
	private DAOMobileApplicationType daoApplication;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {

		if (parameters.containsKey("applicationName")) {
			String applicationName = (String) parameters.get("applicationName");
			if (applicationName != null && applicationName.length() > 0 && isApplicationExisting(applicationName)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isApplicationExisting(String applicationName) {
		try {
			MobileApplicationTypeEntity application = daoApplication.findByName(applicationName);
			if (application != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
	}

	@Override
	public RespCodesEnum getInvalidValueErrorCode() {
		return RespCodesEnum.ERROR_INVALID_APPLICATION_NAME;
	}

}
