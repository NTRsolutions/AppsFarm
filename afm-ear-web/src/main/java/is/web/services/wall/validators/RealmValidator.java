package is.web.services.wall.validators;

import java.util.HashMap;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import is.ejb.bl.business.RespCodesEnum;
import is.ejb.dl.dao.DAORealm;
import is.web.services.APIValidator;

@ManagedBean
public class RealmValidator implements APIValidator {

	@Inject
	private DAORealm daoRealm;

	@Override
	public boolean validate(HashMap<String, Object> parameters) {
		try {
			if (parameters.containsKey("internalNetworkId")) {
				int internalNetworkId = (Integer) parameters.get("internalNetworkId");
				if (internalNetworkId != 0 && daoRealm.findById(internalNetworkId) != null) {
					return true;
				} else {
					return false;
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
		return RespCodesEnum.ERROR_AD_NETWORK_NOT_FOUND;
	}

}
