package is.ejb.bl.business;

import is.ejb.bl.cloudtraxConfiguration.CloudtraxRobot;
import is.ejb.bl.cloudtraxConfiguration.SerDeCloudtraxConfiguration;
import is.ejb.dl.dao.DAOAccessPoints;
import is.ejb.dl.dao.DAOCloudtraxConfiguration;
import is.ejb.dl.dao.DAORadiusConfiguration;
import is.ejb.dl.entities.AccessPointEntity;
import is.ejb.dl.entities.CloudtraxConfigurationEntity;
import is.ejb.dl.entities.RadiusConfigurationEntity;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class CloudtraxManager {

	@Inject
	DAOCloudtraxConfiguration DAOCloudtrax;

	@Inject
	DAORadiusConfiguration DAORadius;

	@Inject
	DAOAccessPoints DAOAccessPoints;

	@Inject
	SerDeCloudtraxConfiguration serDeCloudtrax;

	CloudtraxRobot robot;

	@PostConstruct
	public void init() {
		robot = new CloudtraxRobot();
	}

	public CloudtraxConfigurationEntity getConfigurationWithNetworkName(
			String networkName) {
		try {
			CloudtraxConfigurationEntity entity = DAOCloudtrax
					.findByNetworkName(networkName);
			entity = serDeCloudtrax.deserialize(entity);

			List<RadiusConfigurationEntity> radiusServerConfigList = DAORadius
					.findByCloudtraxId(entity.getId());

			if (radiusServerConfigList.size() == 1) {
				entity.setRadiusServer1Config(radiusServerConfigList.get(0));
				entity.setRadiusServer2Config(radiusServerConfigList.get(0));
			}

			else {
				for (RadiusConfigurationEntity radEntity : radiusServerConfigList) {
					if (radEntity.getIp() == entity.getRadiusServer1())
						entity.setRadiusServer1Config(radEntity);

					if (radEntity.getIp() == entity.getRadiusServer2())
						entity.setRadiusServer2Config(radEntity);
				}

			}
			List<AccessPointEntity> accessPointEntityList = DAOAccessPoints
					.findByCloudtraxId(entity.getId());

			entity.setAccessPointEntityList(accessPointEntityList);

			
			return entity;
		} catch (Exception e) {

			return null;
		}
	}

	public CloudtraxConfigurationEntity getConfigurationWithId(int id) {
		try {
			CloudtraxConfigurationEntity entity = DAOCloudtrax.findById(id);
			entity = serDeCloudtrax.deserialize(entity);

			List<RadiusConfigurationEntity> radiusServerConfigList = DAORadius
					.findByCloudtraxId(entity.getId());

			if (radiusServerConfigList.size() == 1) {
				entity.setRadiusServer1Config(radiusServerConfigList.get(0));
				entity.setRadiusServer2Config(radiusServerConfigList.get(0));
			}

			else {
				for (RadiusConfigurationEntity radEntity : radiusServerConfigList) {
					if (radEntity.getIp() == entity.getRadiusServer1())
						entity.setRadiusServer1Config(radEntity);

					if (radEntity.getIp() == entity.getRadiusServer2())
						entity.setRadiusServer2Config(radEntity);
				}

			}
			List<AccessPointEntity> accessPointEntityList = DAOAccessPoints
					.findByCloudtraxId(entity.getId());

			entity.setAccessPointEntityList(accessPointEntityList);
			System.out.println(entity);
			return entity;
		} catch (Exception e) {

			return null;
		}

	}

	public void synchronizeAllConfigurations() {
		try {
			List<CloudtraxConfigurationEntity> list = DAOCloudtrax.findAll();
			for (CloudtraxConfigurationEntity entity : list) {
				synchronizeConfigurationWithId(entity.getId());
			}
		} catch (Exception exc) {

		}
	}

	public void synchronizeConfigurationWithId(int id) {

		try {
			CloudtraxConfigurationEntity entity = getConfigurationWithId(id);

			HtmlPage page = robot
					.login(entity.getLogin(), entity.getPassword());
			String networkName = robot.getNetworkName(page);
			String radiusServer1 = robot.getRadiusServer1(page);
			String radiusServer2 = robot.getRadiusServer2(page);
			String radiusSecret = robot.getRadiusSecret(page);
			List<String> allowedDomains = robot
					.getCaptivePortalAllowedDomains(page);
			List<String> accessPoints = robot.getAcessPoints(entity.getLogin(),
					entity.getPassword());
			String captivePortalSecret = robot.getCaptivePortalSecret(page);
			String captivePortalServer = robot.getCaptivePortalServer(page);
			String captivePortalURL = robot.getCaptivePortalURL(page);

			entity.setNetworkName(networkName);
			entity.setRadiusServer1(radiusServer1);
			entity.setRadiusServer2(radiusServer2);
			entity.setRadiusSecret(radiusSecret);
			entity.setAllowedDomainsList(allowedDomains);
			entity.setAccessPointsList(accessPoints);
			entity.setCaptivePortalSecret(captivePortalSecret);
			entity.setCaptivePortalServer(captivePortalServer);
			entity.setCaptivePortalURL(captivePortalURL);

			CloudtraxConfigurationEntity ent = serDeCloudtrax.serialize(entity);
			DAOCloudtrax.createOrUpdate(ent);
			System.out.println(ent);
			synchronizeAccessPoints(entity);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void synchronizeAccessPoints(CloudtraxConfigurationEntity entity) {

		List<String> apStringList = entity.getAccessPointsList();

		List<AccessPointEntity> listToRemove = entity
				.getAccessPointEntityList();

		if (apStringList.size() > 0) {

			for (AccessPointEntity apEntity : listToRemove) {
				DAOAccessPoints.delete(apEntity);
			}

			for (String apString : apStringList) {
				AccessPointEntity ap = new AccessPointEntity();
				ap.setCloudtraxId(entity.getId());
				ap.setMac(apString);
				DAOAccessPoints.createOrUpdate(ap);
			}
		}

	}
}
