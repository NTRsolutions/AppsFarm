package ejb.bl.external;

import is.ejb.bl.external.ExternalServerManager;
import is.ejb.bl.external.ExternalServerType;
import is.ejb.dl.dao.DAOExternalServerAddress;
import is.ejb.dl.entities.ExternalServerAddressEntity;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

@RunWith(Arquillian.class)
public class ExternalServerManagerTest {

	@Inject
	ExternalServerManager manager;

	@Deployment
	public static WebArchive createDeployment() {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "test.war").addPackages(true, "web").addPackages(true, "ejb")
				.addAsLibraries(files).addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");

	}

	@Test
	public void test_getall_not_null() {
		Assert.assertEquals(manager.getAll() != null, true);
	}

	@Test
	public void test_get_external_server_address_for_type_not_null() {
		ExternalServerAddressEntity address = manager.getExternalServerAddressForType(ExternalServerType.SUPERSONIC);
		
		Assert.assertEquals(manager.getExternalServerAddressForType(ExternalServerType.SUPERSONIC) != null , true);
	}

	@Test
	public void test_insert_external_server_address() {
		ExternalServerAddressEntity entity = new ExternalServerAddressEntity();
		entity.setIpContent("0.0.0.0");
		entity.setExternalServerType(ExternalServerType.AARKI.toString());

		ExternalServerAddressEntity entityAfterInsert = manager.insertOrUpdateExternalServerAddress(entity);
		Assert.assertEquals(entityAfterInsert != null, true);
		Assert.assertEquals(entityAfterInsert.getId() != 0, true);
		Assert.assertEquals(entityAfterInsert.getIpContent(), "0.0.0.0");

		manager.deleteExternalServerAddress(entityAfterInsert);

	}

	@Test
	public void test_delete_external_server_address() {
		ExternalServerAddressEntity entity = new ExternalServerAddressEntity();
		entity.setIpContent("0.0.0.0");
		entity.setExternalServerType(ExternalServerType.AARKI.toString());
		ExternalServerAddressEntity entityAfterInsert = manager.insertOrUpdateExternalServerAddress(entity);
		Assert.assertEquals(manager.deleteExternalServerAddress(entityAfterInsert), true);
		Assert.assertEquals(manager.deleteExternalServerAddress(null), false);
	}

	@Test
	public void test_is_server_listed() {
		Assert.assertEquals(manager.isServerAddressListed(null, null), false);
		Assert.assertEquals(manager.isServerAddressListed(null, ExternalServerType.PERSONALLY), false);
		Assert.assertEquals(manager.isServerAddressListed(null, ExternalServerType.SUPERSONIC), false);
		ExternalServerAddressEntity entity = new ExternalServerAddressEntity();
		entity.setIpContent("0.0.0.0  192.168.1.1 192.168.25.4 0.0.1.2 0.0.0.0");
		entity.setExternalServerType(ExternalServerType.AARKI.toString());
		entity.setEnabled(true);
		ExternalServerAddressEntity entityAfterInsert = manager.insertOrUpdateExternalServerAddress(entity);
		Assert.assertEquals(manager.isServerAddressListed("0.0.0.0", ExternalServerType.AARKI), true);
		Assert.assertEquals(manager.isServerAddressListed("192.168.1.1", ExternalServerType.AARKI), true);
		Assert.assertEquals(manager.isServerAddressListed("192.168.25.4", ExternalServerType.AARKI), true);
		Assert.assertEquals(entityAfterInsert.getExternalServerType(), "AARKI");
		Assert.assertEquals(entityAfterInsert.isEnabled(), true);
		Assert.assertEquals(manager.isServerAddressListed("192.168.25.0", ExternalServerType.AARKI), false);
		Assert.assertEquals(manager.isServerAddressListed("192.168.25.0", ExternalServerType.HASOFFERS), true);
		Assert.assertEquals(manager.isServerAddressListed("0.0.0.0", null), true);
		Assert.assertEquals(manager.isServerAddressListed("0.0.0.0", ExternalServerType.PERSONALLY), true);
		Assert.assertEquals(manager.isServerAddressListed(null, ExternalServerType.HASOFFERS), false);
		Assert.assertEquals(manager.isServerAddressListed("0.0.0.0", null), true);
		manager.deleteExternalServerAddress(entityAfterInsert);
	}
	
	
	

	
}
