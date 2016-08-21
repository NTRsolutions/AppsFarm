package ejb.bl.friends;

import is.ejb.bl.friends.UserFriendManager;
import is.ejb.dl.entities.UserFriendEntity;

import java.io.File;

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
public class UserFriendManagerTest {
	@Inject
	UserFriendManager manager;

	@Deployment
	public static WebArchive createDeployment() {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "test.war").addPackages(true, "web").addPackages(true, "ejb")
				.addAsLibraries(files).addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");

	}

	/*
	@Test
	public void test_insert_user_friend(){
		
		UserFriendEntity userFriendInsertResult = manager.createUserFriend(11,"Test","765765765");
		Assert.assertEquals(userFriendInsertResult == null, false);
		Assert.assertEquals(userFriendInsertResult.getName(),"Test");
		Assert.assertEquals(userFriendInsertResult.getPhoneNumber(),"765765765");
		Assert.assertEquals(userFriendInsertResult.getUserId(), 11);
		
		manager.removeUserFriend(11, "765765765");
	}
	
	@Test
	public void test_remove_user_friend(){
		UserFriendEntity userFriendInsertResult = manager.createUserFriend(13,"Test","765765765");
		manager.removeUserFriend(13, "765765765");
		Assert.assertEquals(manager.getUserFriendWithPhoneNumber(13, "765765765") == null, true);
	}
	
	@Test
	public void test_get_user_friend_with_phone_number(){
		UserFriendEntity userFriendInsertResult = manager.createUserFriend(14,"Test","765765765");
		Assert.assertEquals(manager.getUserFriendWithPhoneNumber(14, "765765765") != null, true);
		Assert.assertEquals(manager.getUserFriendWithPhoneNumber(14, "7657657651") == null, true);
		Assert.assertEquals(manager.getUserFriendWithPhoneNumber(14, ""	) == null, true);
		Assert.assertEquals(manager.getUserFriendWithPhoneNumber(0, ""	) == null, true);
		manager.removeUserFriend(14, "765765765");
	}
	
	
	@Test
	public void test_is_friend_in_list(){
		UserFriendEntity userFriendInsertResult = manager.createUserFriend(15,"Test","765765765");
		Assert.assertEquals(manager.isFriendInList(15, "765765765") , true);
		Assert.assertEquals(manager.isFriendInList(15, "") , false);
		Assert.assertEquals(manager.isFriendInList(0, "765765765") , false);
		manager.removeUserFriend(15, "765765765");
	}
	
	@Test
	public void test_get_user_friend_list(){
		UserFriendEntity userFriendInsertResult = manager.createUserFriend(16,"Test","765765765");
		Assert.assertEquals(manager.getUserFriendList(16) != null, true);
		Assert.assertEquals(manager.getUserFriendList(0) != null, true);
		Assert.assertEquals(manager.getUserFriendList(16).size() > 0, true);
		Assert.assertEquals(manager.getUserFriendList(0).size() == 0, true);
		manager.removeUserFriend(16, "765765765");
		
	}
	*/
}
