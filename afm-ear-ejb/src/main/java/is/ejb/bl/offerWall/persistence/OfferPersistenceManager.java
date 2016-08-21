package is.ejb.bl.offerWall.persistence;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.OfferProviderCodeNames;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.offerWall.content.OfferWallContent;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.dao.DAOAdProvider;
import is.ejb.dl.dao.DAOOffer;
import is.ejb.dl.dao.DAOOfferWall;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.entities.OfferEntity;
import is.ejb.dl.entities.OfferWallEntity;
import is.ejb.dl.entities.RealmEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class OfferPersistenceManager {

	@Inject
	private Logger logger;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOOffer daoOffer;
	
	public void persistOffers(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls) throws Exception {
		try {
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.OK, 
					"OFFER_WALL_PERSITENCE persisting offers in storage for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId());

			for(int i=0; i<listIndividualOfferWalls.size();i++) {
				IndividualOfferWall wall = listIndividualOfferWalls.get(i);
				ArrayList<Offer> listOffers  = wall.getOffers();
				for(int j=0;j<listOffers.size();j++) {
					Offer offer = listOffers.get(j);

					logger.info("persisting offer: "+offer.getTitle()+" "+offer.getAdProviderCodeName());

					OfferEntity offerEntity = new OfferEntity();
					offerEntity.setOfferId(offer.getId());
					offerEntity.setAffiliateId(offer.getAffiliateId());
					offerEntity.setSourceId(offer.getSourceId());
					offerEntity.setName(offer.getTitle());
					offerEntity.setUrl(offer.getUrl());
					offerEntity.setAdProviderCodeName(offer.getAdProviderCodeName());
					offerEntity.setOfferId(offer.getId());

					offerEntity.setPayout(offer.getPayout());
					offerEntity.setPayoutIsoCurrencyCode(offer.getCurrency());
					offerEntity.setRewardCurrency(offer.getRewardCurrency());
					offerEntity.setRewardValue(offer.getRewardValue());
					offerEntity.setRevenueSplitValue(offer.getRevenueSplitValue());
					offerEntity.setProfitValue(offer.getProfitValue());
					
					offerEntity.setRewardType(offer.getRewardType());
					offerEntity.setOfferPayoutInTargetCurrency(offer.getPayoutInTargetCurrency());
					offerEntity.setRevenueValue(offer.getPayoutInTargetCurrency());
					offerEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
					offerEntity.setNetworkId(offer.getInternalNetworkId());
					
					offerEntity.setDescription(offer.getDescription());
//					if(offer.getCallToAction() != null && offer.getCallToAction().length()>250) {
//						offer.setCallToAction(offer.getCallToAction().substring(0, 250));
//					}
					offer.setCallToAction(offer.getCallToAction());

//					if(offer.getPreviewUrl() != null && offer.getPreviewUrl().length()>250) {
//						offer.setPreviewUrl(offer.getPreviewUrl().substring(0, 250));
//					}
					offer.setPreviewUrl(offer.getPreviewUrl());
					
					try {
						daoOffer.create(offerEntity);
					} catch(Exception exc) {
						logger.info("error: "+exc.toString());
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
								LogStatus.ERROR, 
								"OFFER_WALL_PERSISTENCE error persisting offer: "+offer.getTitle()+" for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId()+" error: "+exc.toString());
					}
				}
			}
		} 
		catch (Exception exc) {
			exc.printStackTrace();
			logger.info("error: "+exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					"OFFER_WALL_PERSISTENCE error persisting offers for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId());
			throw new Exception(exc.toString());
		}
	}

	public void persistOffers(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls,
			String providerType) throws Exception {
		try {
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.OK, 
					"OFFER_WALL_PERSITENCE persisting offers in storage for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId());

			for(int i=0; i<listIndividualOfferWalls.size();i++) {
				IndividualOfferWall wall = listIndividualOfferWalls.get(i);
				if(wall.getAdProviderCodeName().toUpperCase().equals(providerType.toUpperCase())) {
					ArrayList<Offer> listOffers  = wall.getOffers();
					for(int j=0;j<listOffers.size();j++) {
						Offer offer = listOffers.get(j);

						logger.info("persisting offer: "+offer.getTitle()+" "+offer.getAdProviderCodeName());

						OfferEntity offerEntity = new OfferEntity();
						offerEntity.setOfferId(offer.getId());
						offerEntity.setAffiliateId(offer.getAffiliateId());
						offerEntity.setSourceId(offer.getSourceId());
						offerEntity.setName(offer.getTitle());
						offerEntity.setUrl(offer.getUrl());
						
						offerEntity.setAdProviderCodeName(offer.getAdProviderCodeName());
						offerEntity.setOfferId(offer.getId());

						offerEntity.setPayout(offer.getPayout());
						offerEntity.setPayoutIsoCurrencyCode(offer.getCurrency());
						offerEntity.setRewardCurrency(offer.getRewardCurrency());
						offerEntity.setRewardValue(offer.getRewardValue());
						offerEntity.setRevenueSplitValue(offer.getRevenueSplitValue());
						offerEntity.setProfitValue(offer.getProfitValue());
						
						offerEntity.setRewardType(offer.getRewardType());
						offerEntity.setOfferPayoutInTargetCurrency(offer.getPayoutInTargetCurrency());
						offerEntity.setRevenueValue(offer.getPayoutInTargetCurrency());
						offerEntity.setGenerationDate(new Timestamp(System.currentTimeMillis()));
						offerEntity.setNetworkId(offer.getInternalNetworkId());
						
						offerEntity.setDescription(offer.getDescription());
						
//						if(offer.getCallToAction() != null && offer.getCallToAction().length()>250) {
//							offer.setCallToAction(offer.getCallToAction().substring(0, 250));
//						}
						offer.setCallToAction(offer.getCallToAction());

//						if(offer.getPreviewUrl() != null && offer.getPreviewUrl().length()>250) {
//							offer.setPreviewUrl(offer.getPreviewUrl().substring(0, 250));
//						}
						offer.setPreviewUrl(offer.getPreviewUrl());

						try {
							daoOffer.create(offerEntity);
						} catch(Exception exc) {
							logger.info("error: "+exc.toString());
							Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
									LogStatus.ERROR, 
									"OFFER_WALL_PERSISTENCE error persisting offer: "+offer.getTitle()+" for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId()+" error: "+exc.toString());
						}
					}
				}
			}
		} 
		catch (Exception exc) {
			exc.printStackTrace();
			logger.info("error: "+exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, offerWall.getRealm().getId(), 
					LogStatus.ERROR, 
					"OFFER_WALL_PERSISTENCE error persisting offers for offer wall: "+offerWall.getName()+" realm: "+offerWall.getRealm().getName()+" id: "+offerWall.getRealm().getId());
			throw new Exception(exc.toString());
		}
	}

}
