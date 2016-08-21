package is.ejb.bl.external;

import is.ejb.bl.business.Application;
import is.ejb.bl.external.ExternalServerType;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.entities.ExternalServerAddressEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ExternalServerManager {

	@Inject
	private Logger logger;
	@Inject
	private DAOExternalServerAddress daoExternalServerAddress;

	public List<ExternalServerAddressEntity> getAll() {
		try {
			return daoExternalServerAddress.findAll();
		} catch (Exception exception) {
			exception.toString();
			return new ArrayList<ExternalServerAddressEntity>();
		}
	}

	public ExternalServerAddressEntity getExternalServerAddressForType(ExternalServerType type) {
		try {
			return daoExternalServerAddress.findByExternalServerType(type);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public boolean isServerAddressListedWithType(String ipAddress, String type) {
		logger.info("Checking is server address listed: " + ipAddress + " in type: " + type);
		return isServerAddressListed(ipAddress, ExternalServerType.valueOf(type));
	}

	public boolean isServerAddressListed(String ipAddress, ExternalServerType type) {

		boolean result = false;
		if (ipAddress != null) {
			ExternalServerAddressEntity entity = getExternalServerAddressForType(type);
			if (entity != null && entity.isEnabled() == true) {
				if (entity.getIpContent() != null) {
					if (entity.getIpContent().contains(ipAddress)) {
						result = true;
					}
				}
			} else {
				logger.info("No entity for type:" + type + " or checking is disabled.");
				result = true;
			}
		}
		logger.info("Server: " + ipAddress + " in type: " + type + " is listed?:" + result);
		Application.getElasticSearchLogger().indexLog(Application.EXTERNAL_SERVER_MANAGER_ACTIVITY, -1, LogStatus.OK,
				Application.EXTERNAL_SERVER_MANAGER_ACTIVITY + " checking is server address listed for ipAddress: "
						+ ipAddress + " type: " + type.toString() + " result: " + result);

		return result;
	}

	public ExternalServerAddressEntity insertOrUpdateExternalServerAddress(ExternalServerAddressEntity entity) {
		Application.getElasticSearchLogger().indexLog(Application.EXTERNAL_SERVER_MANAGER_ACTIVITY, -1, LogStatus.OK,
				Application.EXTERNAL_SERVER_MANAGER_ACTIVITY + " inserting new / updating external server address: "
						+ entity);

		return daoExternalServerAddress.createOrUpdate(entity);
	}

	public boolean deleteExternalServerAddress(ExternalServerAddressEntity entity) {
		try {
			Application.getElasticSearchLogger().indexLog(Application.EXTERNAL_SERVER_MANAGER_ACTIVITY, -1,
					LogStatus.OK,
					Application.EXTERNAL_SERVER_MANAGER_ACTIVITY + " deletingexternal server address: " + entity);
			daoExternalServerAddress.delete(entity);
			return true;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}

}
