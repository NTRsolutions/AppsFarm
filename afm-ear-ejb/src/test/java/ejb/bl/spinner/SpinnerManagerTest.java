package ejb.bl.spinner;

import is.ejb.bl.spinner.SpinnerManager;
import is.ejb.dl.entities.UserEventEntity;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SpinnerManagerTest {

	@Inject
	private SpinnerManager spinnerManager;
	
	@Deployment
	public static WebArchive createDeployment() {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "test.war").addPackages(true, "web").addPackages(true, "ejb")
				.addAsLibraries(files).addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");

	}
	
	@Test
	public void test_select_spinner_rewards_in_range(){
		Calendar calendar = Calendar.getInstance(); 
		calendar.add(Calendar.DAY_OF_MONTH, -30);
		Timestamp startDate = new Timestamp(calendar.getTimeInMillis());
		Timestamp endDate = new Timestamp(new Date().getTime());
		List<UserEventEntity> events = spinnerManager.selectSpinnerRewardsInDateRange(startDate, endDate);
		Assert.assertEquals(events == null,false);
		Assert.assertEquals(events.size() > 0, true);
		
	}
}
