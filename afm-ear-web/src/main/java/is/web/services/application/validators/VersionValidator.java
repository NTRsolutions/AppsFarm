package is.web.services.application.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOMobileApplicationType;
import is.ejb.dl.entities.MobileApplicationTypeEntity;
import is.web.services.APIValidator;

@ManagedBean
public class VersionValidator implements APIValidator {

	@Inject
	private DAOMobileApplicationType daoApplication;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			if (parameters.containsKey("version")) {
				String version = (String) parameters.get("version");
				String applicationName = (String) parameters.get("applicationName");
				MobileApplicationTypeEntity mobileApplication = daoApplication.findByName(applicationName);
				if (mobileApplication.isVersionCheck()) {
					String minimumVersion = mobileApplication.getMinimumVersion();
					if (validateVersion(minimumVersion, version)) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
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
		return RespCodesEnum.ERROR_INVALID_VERSION;
	}

	private boolean validateVersion(String minimumVersion, String versionNumber) {
		if (versionNumber.split("\\.").length != 3) {

			return false;
		}

		String[] minimumVersionArray = minimumVersion.split("\\.");
		int[] minimumVersionNumberArray = new int[minimumVersionArray.length];
		for (int i = 0; i < minimumVersionArray.length; i++) {

			if (minimumVersionArray[i].length() > 3)
				minimumVersionNumberArray[i] = Integer.parseInt(minimumVersionArray[i].substring(0, 3));
			else

				minimumVersionNumberArray[i] = Integer.parseInt(minimumVersionArray[i]);
		}

		String[] userVersionArray = versionNumber.split("\\.");
		int[] userVersionNumberArray = new int[userVersionArray.length];
		for (int i = 0; i < userVersionArray.length; i++) {

			if (userVersionArray[i].length() > 3)
				userVersionNumberArray[i] = Integer.parseInt(userVersionArray[i].substring(0, 3));
			else
				userVersionNumberArray[i] = Integer.parseInt(userVersionArray[i]);
		}

		// a.bb.ccc
		// checking "a"
		if (minimumVersionNumberArray[0] > userVersionNumberArray[0]) {
			return false;
		} else if (minimumVersionNumberArray[0] < userVersionNumberArray[0]) {
			return true;

		}

		// checking "bb"
		if (minimumVersionNumberArray[1] > userVersionNumberArray[1]) {
			return false;
		} else if (minimumVersionNumberArray[1] < userVersionNumberArray[1]) {
			return true;
		}

		// checking "ccc"
		if (minimumVersionNumberArray[2] > userVersionNumberArray[2]) {
			return false;
		} else if (minimumVersionNumberArray[2] <= userVersionNumberArray[2]) {
			return true;

		}

		return false;
	}

}
