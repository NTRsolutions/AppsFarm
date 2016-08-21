package is.ejb.bl.business;

import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.UserEntity;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class CustomerManager {

   @Inject
   private EntityManager em;

   @Inject
   private Logger logger;

   @Inject
   DAOUser daoCustomer;

   //this generates customer account with associated default settings for domain, node and node configuration
   //once this is created, nodes can automatically register
   /*
   public void createDefaultCustomerRealm()
   {
	   try {
		   daoNodeDomain.findByName(SystemParameters.defaultConfigurationDomain);
		   logger.info("Default domain exists, skipping registration...");
	   } catch(Exception exc) {
		   logger.info("Unable to find default domain setup, registering default domain and associated data...");
		   logger.info(exc.toString());
		   //exc.printStackTrace();
		   
		   //------------------------- generate test node domains -----------------------
		   //entity 1
		   NodeDomainEntity entity1 = new NodeDomainEntity();
		   entity1.setName(SystemParameters.defaultConfigurationDomain);
		   //if no credentials supplied then any node can register to this domain
		   entity1.setDefaultDomain(true);
		   entity1.setNodeAccessLogin("admin");
		   entity1.setNodeAccessPassword("admin");
		   //create
		   daoNodeDomain.create(entity1);
		   listNodeDomains.add(entity1);

		   //create example Domain 1
		   entity1 = new NodeDomainEntity();
		   entity1.setName("Configuration domain 1");
		   //if no credentials supplied then any node can register to this domain
		   entity1.setDefaultDomain(true);
		   entity1.setNodeAccessLogin("admin");
		   entity1.setNodeAccessPassword("admin");
		   //create
		   daoNodeDomain.create(entity1);
		   listNodeDomains.add(entity1);

		   //create example Domain 1
		   entity1 = new NodeDomainEntity();
		   entity1.setName("Configuration domain 2");
		   //if no credentials supplied then any node can register to this domain
		   entity1.setDefaultDomain(true);
		   entity1.setNodeAccessLogin("admin");
		   entity1.setNodeAccessPassword("admin");
		   //create
		   daoNodeDomain.create(entity1);
		   listNodeDomains.add(entity1);

		   //------------------------- generate test customer account -----------------------
		   CustomerEntity entity = new CustomerEntity();
		   entity.setName("BT");
		   entity.setEmail("bt@gmail.com");
		   entity.setLogin("admin");
		   entity.setPassword("admin");
		   entity.setNodeDomains(listNodeDomains);
		   //create
		   daoCustomer.create(entity);
		   
		   logger.info("created default customer realm...");
	   }
   }
   */
   //check if not is already registered to any of the domains - if not initiate registration process to default domain
   /*
   public boolean isNodeRegistered(HeartbeatDataHolder hbDataHolder)
   {
	   //check if node exists within the system
	   try {
		   daoNode.findByName(hbDataHolder.getNodeName());
	   } catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   return false;
   }
   */
   
   public void createTestCustomerRealm()
   {
//	   //config entity 1
//	   NodeConfigurationEntity entityConfiguration = new NodeConfigurationEntity();
//	   entityConfiguration.setName("NodeConfiguration");
//	   entityConfiguration.setNodeAccessLogin("node");
//	   entityConfiguration.setNodeAccessPassword("node");
//	   entityConfiguration.setNodeConfigurationEndpointUrl("http://localhost:5681/sentinel/ConfigurationService");
//	   entityConfiguration.setServerHeartbeatEndpointUrl("http://localhost:5680/iWeb/HeartBeatService");
//	   //create
//	   daoNodeConfiguration.create(entityConfiguration);
//
//	   //node entity 1
//	   NodeEntity entity = new NodeEntity();
//	   entity.setName("DNode-1");
//	   entity.setConfiguration(entityConfiguration);
//	   //create
//	   dao.create(entity);
//	   
   }

   public void registerNode()
   {
	   
   }

   public void configureNode()
   {
   }
}
