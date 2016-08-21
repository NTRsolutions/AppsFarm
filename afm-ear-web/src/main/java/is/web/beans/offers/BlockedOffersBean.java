package is.web.beans.offers;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.BlockedOfferType;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerFilter.BlockedOffer;
import is.ejb.bl.offerFilter.BlockedOffers;
import is.ejb.bl.offerFilter.SerDeBlockedOffers;
import is.ejb.bl.offerProviders.aarki.AarkiProviderConfig;
import is.ejb.bl.offerProviders.aarki.SerDeAarkiProviderConfiguration;
import is.ejb.bl.offerProviders.fyber.FyberProviderConfig;
import is.ejb.bl.offerProviders.fyber.SerDeFyberProviderConfiguration;
import is.ejb.bl.offerProviders.hasoffers.HasoffersProviderConfig;
import is.ejb.bl.offerProviders.hasoffers.SerDeHasoffersProviderConfiguration;
import is.ejb.bl.offerProviders.minimob.MinimobProviderConfig;
import is.ejb.bl.offerProviders.minimob.SerDeMinimobProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SerDeSupersonicProviderConfiguration;
import is.ejb.bl.offerProviders.supersonic.SupersonicProviderConfig;
import is.ejb.bl.offerWall.config.SerDeOfferWallConfiguration;
import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.offerWall.content.SerDeOfferWallContent;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOBlockedOffers;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.dao.DAOUser;
import is.ejb.dl.entities.AdProviderEntity;
import is.ejb.dl.entities.BlockedOffersEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.UserEntity;
import is.web.beans.users.LoginBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.lucene.analysis.compound.hyphenation.TernaryTree.Iterator;
import org.primefaces.context.RequestContext;

@ManagedBean(name="blockedOffersBean")
@SessionScoped
public class BlockedOffersBean implements Serializable {

	@Inject
	private Logger logger;
	
	private LoginBean loginBean;

	//blocked offers
	@Inject
	private DAOBlockedOffers daoBlockedOffers;
	@Inject
	private SerDeBlockedOffers serDeBlockedOffers;
	private BlockedOffersEntity blockedOffersEntity = null;
	private BlockedOffers blockedOffers = null;
	private ArrayList<BlockedOffer> listBlockedOffers = null;

	//multi-offer walls (mows)
	@Inject
	private DAOOfferWall daoOfferWall;
	@Inject
	private SerDeOfferWallContent serDeOfferWallContent;
	private List<OfferWallEntity> listMows = new ArrayList<OfferWallEntity>();
	private ArrayList<SingleOfferWallConfiguration> listSingleOfferWallConfigurations = new ArrayList<SingleOfferWallConfiguration>();
	
	private BlockedOffersDataModelBean blockedoffersDataModel = null;
	
	private ArrayList<Offer> listAllAvctiveIndividualOffers = new ArrayList<Offer>();
	private String selectedOfferToBlockKey = null;
	private Offer selectedOfferToBlock = null;
	//IndividualOfferWall individualOfferWall = new IndividualOfferWall();
	
	@Inject
	private DAORealm daoRealm;
	
	private UserEntity customer;

	private BlockedOffer editedBlockedOffer;

	private List<RewardTypeEntity> listRewardTypes = new ArrayList<RewardTypeEntity>();

	@Inject
	private DAORewardType daoRewardType;

	private String rewardTypeName = "";
	
	public BlockedOffersBean() {
	}
	
   @PostConstruct
   public void init() {
	   FacesContext fc = FacesContext.getCurrentInstance();
	   loginBean = (LoginBean)  fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
	   //create fake one
	   editedBlockedOffer = new BlockedOffer();
	   editedBlockedOffer.setTitle("");
	   editedBlockedOffer.setActive(true);
	   editedBlockedOffer.setAdProviderCodeName("");
	   editedBlockedOffer.setBlockType(BlockedOfferType.MANUAL.toString());
	   editedBlockedOffer.setCommand("");
	   editedBlockedOffer.setConvRatio(0);
	   editedBlockedOffer.setRenderConversionStats(false);

	   refresh();
   }
  
   public void persistConfiguration() {
		try {
			logger.info("persisting configuration");
			blockedOffersEntity = daoBlockedOffers.findByRealmId(loginBean.getUser().getRealm().getId());
			BlockedOffers blockedOffers = new BlockedOffers();
			blockedOffers.setListBlockedOffers(listBlockedOffers);

			String strBlockedOffers = serDeBlockedOffers.serialize(blockedOffers);
			blockedOffersEntity.setContent(strBlockedOffers);
			blockedOffersEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
			blockedOffersEntity = daoBlockedOffers.createOrUpdate(blockedOffersEntity);

		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			listBlockedOffers = new ArrayList<BlockedOffer>();
			blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
					"Failed", "Unable to persist bloced offers configuration: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
   }
   
   public void refresh() {
		try {
			logger.info("refreshing bean...");
			blockedOffersEntity = daoBlockedOffers.findByRealmId(loginBean.getUser().getRealm().getId());
			if(blockedOffersEntity == null) { //of no blocked offers entry exists - create new one
				logger.info("creating new empty blocked offers entity!");
				listBlockedOffers = new ArrayList<BlockedOffer>();
				blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
				//create object if it did not exist before
				blockedOffersEntity = new BlockedOffersEntity();
				blockedOffersEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
				blockedOffersEntity.setRealmId(loginBean.getUser().getRealm().getId());

				BlockedOffers blockedOffers = new BlockedOffers();
				blockedOffers.setListBlockedOffers(listBlockedOffers);

				String strBlockedOffers = serDeBlockedOffers.serialize(blockedOffers);
				blockedOffersEntity.setContent(strBlockedOffers);
				blockedOffersEntity = daoBlockedOffers.createOrUpdate(blockedOffersEntity);
			}
			
			blockedOffers = serDeBlockedOffers.deserialize(blockedOffersEntity.getContent());
			listBlockedOffers = blockedOffers.getListBlockedOffers();
			blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
			
			//find all active offer walls
			listAllAvctiveIndividualOffers = new ArrayList<Offer>();
			List<OfferWallEntity> listOfferWalls = (List<OfferWallEntity>)daoOfferWall.findAllByRealmIdAndActive(loginBean.getUser().getRealm().getId(), true);
			for(int i=0;i<listOfferWalls.size();i++) {
				OfferWallEntity mow = listOfferWalls.get(i);
				OfferWallContent offerWallContent = serDeOfferWallContent.deserialize(mow.getContent());
				ArrayList<IndividualOfferWall> listIndividualOfferWals = offerWallContent.getOfferWalls();
				for(int j=0;j<listIndividualOfferWals.size();j++) {
					IndividualOfferWall individualOfferWall = listIndividualOfferWals.get(j);
					ArrayList<Offer> listOffers = individualOfferWall.getOffers();
					for(int k=0;k<listOffers.size();k++) {
						Offer offer = listOffers.get(k);
						//add offer
						//this is blocking from displaying all offers with duplicate names
						//if(!isOfferAlreadyAdded(listAllAvctiveIndividualOffers, offer)) {
						//	listAllAvctiveIndividualOffers.add(offer);
						//}
						//here we display only offers that have specific rewardType
						logger.info("rewardType name: "+offer.getRewardType());
						if(offer.getRewardType().equals(rewardTypeName)) {
							listAllAvctiveIndividualOffers.add(offer);							
						}
					}
				}
			}

			listRewardTypes = daoRewardType.findAllByRealmId(loginBean.getUser().getRealm().getId());
			logger.info("identified rewardTypes number: "+listRewardTypes.size());

			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idTableBlockedOffers");
			RequestContext.getCurrentInstance().update("tabView:idSelectOffersToBlock");
			
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
	}

    public void displayOffersToBlock() {
		try {
			logger.info("refreshing list of offers to block that belong to rewardType: "+rewardTypeName);
			blockedOffersEntity = daoBlockedOffers.findByRealmId(loginBean.getUser().getRealm().getId());
			if(blockedOffersEntity == null) { //of no blocked offers entry exists - create new one
				logger.info("creating new empty blocked offers entity!");
				listBlockedOffers = new ArrayList<BlockedOffer>();
				blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
				//create object if it did not exist before
				blockedOffersEntity = new BlockedOffersEntity();
				blockedOffersEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
				blockedOffersEntity.setRealmId(loginBean.getUser().getRealm().getId());

				BlockedOffers blockedOffers = new BlockedOffers();
				blockedOffers.setListBlockedOffers(listBlockedOffers);

				String strBlockedOffers = serDeBlockedOffers.serialize(blockedOffers);
				blockedOffersEntity.setContent(strBlockedOffers);
				blockedOffersEntity = daoBlockedOffers.createOrUpdate(blockedOffersEntity);
			}
			
			blockedOffers = serDeBlockedOffers.deserialize(blockedOffersEntity.getContent());
			listBlockedOffers = blockedOffers.getListBlockedOffers();
			blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
			
			//find all active offer walls
			listAllAvctiveIndividualOffers = new ArrayList<Offer>();
			List<OfferWallEntity> listOfferWalls = (List<OfferWallEntity>)daoOfferWall.findAllByRealmIdAndActive(loginBean.getUser().getRealm().getId(), true);
			for(int i=0;i<listOfferWalls.size();i++) {
				OfferWallEntity mow = listOfferWalls.get(i);
				OfferWallContent offerWallContent = serDeOfferWallContent.deserialize(mow.getContent());
				ArrayList<IndividualOfferWall> listIndividualOfferWals = offerWallContent.getOfferWalls();
				for(int j=0;j<listIndividualOfferWals.size();j++) {
					IndividualOfferWall individualOfferWall = listIndividualOfferWals.get(j);
					ArrayList<Offer> listOffers = individualOfferWall.getOffers();
					for(int k=0;k<listOffers.size();k++) {
						Offer offer = listOffers.get(k);
						//add offer
						//this is blocking from displaying all offers with duplicate names
						//if(!isOfferAlreadyAdded(listAllAvctiveIndividualOffers, offer)) {
						//	listAllAvctiveIndividualOffers.add(offer);
						//}
						//here we display only offers that have specific rewardType
						logger.info("rewardType name: "+offer.getRewardType());
						if(offer.getRewardType().equals(rewardTypeName)) {
							listAllAvctiveIndividualOffers.add(offer);							
						}
					}
				}
			}

			listRewardTypes = daoRewardType.findAllByRealmId(loginBean.getUser().getRealm().getId());
			logger.info("identified rewardTypes number: "+listRewardTypes.size());

			//refresh tab GUI after model update
			RequestContext.getCurrentInstance().update("tabView:idTableBlockedOffers");
			RequestContext.getCurrentInstance().update("tabView:idSelectOffersToBlock");
		} catch (Exception e) {
			//handle exception - show empty list of scripts 
			e.printStackTrace();
			logger.severe("Error: "+e.toString());
		}
    }
    
   
    private boolean isOfferAlreadyAdded(ArrayList<Offer> listAllOffers, Offer offerToAdd) {
    	for(int i=0;i<listAllOffers.size();i++) {
    		Offer alreadyAddedOffer = listAllOffers.get(i);
    		if(alreadyAddedOffer.getTitle() != null && 
    				offerToAdd.getTitle() != null &&
    				alreadyAddedOffer.getTitle().equals(offerToAdd.getTitle()) &&
    				alreadyAddedOffer.getAdProviderCodeName().equals(offerToAdd.getAdProviderCodeName())) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
	public void blockOffer() {
		System.out.println("!!! "+selectedOfferToBlockKey+" rewardTypeName: "+rewardTypeName);
		try {
			String offerProvider = selectedOfferToBlockKey.substring(0, selectedOfferToBlockKey.indexOf("-")).trim();
			String offerSourceId = selectedOfferToBlockKey.substring(selectedOfferToBlockKey.indexOf("-")+1, selectedOfferToBlockKey.length()).trim();
			logger.info("offer provider: "+offerProvider+" offer source Id: "+offerSourceId);
			for(int i=0;i<listAllAvctiveIndividualOffers.size();i++) {
				Offer offer = listAllAvctiveIndividualOffers.get(i);
				logger.info(offer.getAdProviderCodeName()+"-"+offerProvider+" "+offer.getSourceId()+"-"+offerSourceId+" rt: "+offer.getRewardType());
				if(offer.getAdProviderCodeName().equals(offerProvider) && offer.getSourceId().equals(offerSourceId)) {
					//logger.info("found!");
					selectedOfferToBlock = offer;
					break;
				}
			}
			
			if(selectedOfferToBlock != null) {
				logger.info("blocking offer: "+selectedOfferToBlock.getTitle()+" ad provider: "+selectedOfferToBlock.getAdProviderCodeName());
				addOfferToBlockList(selectedOfferToBlock);
			}
			
			//refresh the list of offers to block as we added one to be blocked
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"Failed", "Unable to block selected offer: "+selectedOfferToBlock.getTitle()+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
			refresh();
		}
	}

	private void addOfferToBlockList(Offer offerToBlock) {
		for(int i=0;i<listBlockedOffers.size();i++) {
			BlockedOffer bo = listBlockedOffers.get(i);
		
			if(bo.getSourceId() != null && bo.getSourceId().length() > 0 &&
				bo.getSourceId().equals(offerToBlock.getSourceId()) && 
					bo.getAdProviderCodeName().equals(offerToBlock.getAdProviderCodeName()) &&
						bo.getRewardType().equals(offerToBlock.getRewardType())) {
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
						"Failed", "Offer: "+offerToBlock.getTitle()+" already added to the bocking list"));
				RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
				return;
			}
		}
		
		logger.info("adding new offer to block");
		BlockedOffer newBlockedOffer = new BlockedOffer();
		newBlockedOffer.setActive(true);
		newBlockedOffer.setAdProviderCodeName(offerToBlock.getAdProviderCodeName());
		newBlockedOffer.setRewardType(offerToBlock.getRewardType());
		newBlockedOffer.setId(offerToBlock.getId());
		newBlockedOffer.setSourceId(offerToBlock.getSourceId());
		newBlockedOffer.setTitle(offerToBlock.getTitle());
		newBlockedOffer.setTimestamp(new Timestamp(System.currentTimeMillis()));
		newBlockedOffer.setBlockType(BlockedOfferType.MANUAL.toString());

		listBlockedOffers.add(newBlockedOffer);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
				"Success", "Offer: "+selectedOfferToBlock.getTitle()+" successfully blocked"));
		RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
		
		//store current configuration in db
		persistConfiguration();
		
		//refresh blocked offers table 
		blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
		RequestContext.getCurrentInstance().update("tabView:idTableBlockedOffers");
	}
	
	public void delete(BlockedOffer blockedOffer) {
		logger.info("removing offer: "+blockedOffer.getTitle()+" from blocked offers list");
		
		try {
			for(int i=0;i<listBlockedOffers.size();i++){
				BlockedOffer bo = listBlockedOffers.get(i);
				if(bo.getSourceId() != null && bo.getSourceId().length() > 0) {
					if(bo.getSourceId().equals(blockedOffer.getSourceId()) &&
							bo.getAdProviderCodeName().equals(blockedOffer.getAdProviderCodeName()) &&
								bo.getTitle().equals(blockedOffer.getTitle()) ) {
						
							listBlockedOffers.remove(i);
							//store current configuration in db
							persistConfiguration();
							//refresh blocked offers table 
							blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
							RequestContext.getCurrentInstance().update("tabView:idTableBlockedOffers");
							break;
					}
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to remove blocking: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
			refresh();
		}
	}

	public void setEditedOffer(BlockedOffer blockedOffer) {
		logger.info("editing blocked offer: "+blockedOffer.getTitle());
		editedBlockedOffer = blockedOffer;
	}

	public void updateEditedOffer() {
		logger.info("updating edited offer: "+editedBlockedOffer.getTitle());
		
		try {
			for(int i=0;i<listBlockedOffers.size();i++) {
				BlockedOffer bo = listBlockedOffers.get(i);
				if(bo.getSourceId().equals(editedBlockedOffer.getSourceId()) &&
						bo.getAdProviderCodeName().equals(editedBlockedOffer.getAdProviderCodeName()) ) {
					bo.setActive(editedBlockedOffer.isActive()); //set offer blocking state based on edited value
				}
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
					"Success", "Offer: "+editedBlockedOffer.getTitle()+" successfully updated"));
			RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
			
			//store current configuration in db
			persistConfiguration();
			
			//refresh blocked offers table 
			blockedoffersDataModel = new BlockedOffersDataModelBean(listBlockedOffers);
			RequestContext.getCurrentInstance().update("tabView:idTableBlockedOffers");

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Unable to update offer: "+editedBlockedOffer.getTitle()+" Error: "+e.toString()));
			RequestContext.getCurrentInstance().update("tabView:idBlockedOffersGrowl");
			refresh();
		}
	}

	public BlockedOffersDataModelBean getBlockedoffersDataModel() {
		return blockedoffersDataModel;
	}

	public void setBlockedoffersDataModel(
			BlockedOffersDataModelBean blockedoffersDataModel) {
		this.blockedoffersDataModel = blockedoffersDataModel;
	}

	public ArrayList<Offer> getListAllAvctiveIndividualOffers() {
		return listAllAvctiveIndividualOffers;
	}

	public void setListAllAvctiveIndividualOffers(
			ArrayList<Offer> listAllAvctiveIndividualOffers) {
		this.listAllAvctiveIndividualOffers = listAllAvctiveIndividualOffers;
	}

	public Offer getSelectedOfferToBlock() {
		return selectedOfferToBlock;
	}

	public void setSelectedOfferToBlock(Offer selectedOfferToBlock) {
		this.selectedOfferToBlock = selectedOfferToBlock;
	}

	public String getSelectedOfferToBlockKey() {
		return selectedOfferToBlockKey;
	}

	public void setSelectedOfferToBlockKey(String selectedOfferToBlockKey) {
		this.selectedOfferToBlockKey = selectedOfferToBlockKey;
	}

	public BlockedOffer getEditedBlockedOffer() {
		return editedBlockedOffer;
	}

	public void setEditedBlockedOffer(BlockedOffer editedBlockedOffer) {
		this.editedBlockedOffer = editedBlockedOffer;
	}

	public List<RewardTypeEntity> getListRewardTypes() {
		return listRewardTypes;
	}

	public void setListRewardTypes(List<RewardTypeEntity> listRewardTypes) {
		this.listRewardTypes = listRewardTypes;
	}

	public String getRewardTypeName() {
		return rewardTypeName;
	}

	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}

	
	
}

