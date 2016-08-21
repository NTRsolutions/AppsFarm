package is.ejb.bl.offerWall;


import is.ejb.bl.business.Application;
import is.ejb.bl.offerWall.content.IndividualOfferWall;
import is.ejb.bl.offerWall.content.Offer;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.dl.entities.OfferWallEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.inject.Inject;

public class OfferDuplicatesDetector {

	@Inject
	private Logger logger;

	private ArrayList<Offer> listAddedOffers = new ArrayList<Offer>();
	private ArrayList<Offer> listDuplicateOffersToReject = new ArrayList<Offer>();
	
	//------------------------------- global filter ------------------------------------------------
	public void rememberAddedOffer(Offer addedOffer, OfferWallEntity offerWall) {
//		String message = Application.SINGLE_OFFER_DUPLICATES_FILTERING+" "+
//				" adding new offer to pool of selected offers: "+
//				" existing offer: "+addedOffer.getTitle()+" payout in target currency: "+addedOffer.getPayoutInTargetCurrency()+" "+addedOffer.getAdProviderCodeName(); 
//		Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
//				offerWall.getRealm().getId(), 
//				LogStatus.WARNING, 
//				message);

		if(addedOffer !=null && addedOffer.getTitle()!=null && addedOffer.getTitle().length()>0) {
			listAddedOffers.add(addedOffer);
		}
	}
	
	public boolean isOfferDuplicate(Offer newOfferToAdd, OfferWallEntity offerWall) {
		for(int i=0;i<listAddedOffers.size();i++) {
			Offer addedOffer = listAddedOffers.get(i);
			if(newOfferToAdd.getTitle() != null && addedOffer.getTitle().trim().equals(newOfferToAdd.getTitle().trim())) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isOfferDuplicateAndWithHigherPayout(Offer newOfferToAdd, OfferWallEntity offerWall) {
		//logger.info("!!!!!!!!!! "+listAddedOffers.size());

		for(int i=0;i<listAddedOffers.size();i++) {
			Offer addedOffer = listAddedOffers.get(i);
			if(addedOffer.getTitle().trim().equals(newOfferToAdd.getTitle().trim())) {

				//logger.info("+++++++++++++++++++++ existing offer: "+addedOffer.getTitle()+" payout in target currency: "+addedOffer.getPayoutInTargetCurrency()+" "+addedOffer.getAdProviderCodeName()+
				//			" new offer to add: "+newOfferToAdd.getTitle()+" payout in target currency: "+newOfferToAdd.getPayoutInTargetCurrency()+" "+newOfferToAdd.getAdProviderCodeName());
				
				if(addedOffer.getPayoutInTargetCurrency() < newOfferToAdd.getPayoutInTargetCurrency()) {
					//logger.info("++++!!!!!!!!!!!!!!!!!!!!!+++++++++++++++++ existing offer: "+addedOffer.getTitle()+" payout in target currency: "+addedOffer.getPayoutInTargetCurrency()+" "+addedOffer.getAdProviderCodeName()+
					//		" new offer to add: "+newOfferToAdd.getTitle()+" payout in target currency: "+newOfferToAdd.getPayoutInTargetCurrency()+" "+newOfferToAdd.getAdProviderCodeName());
				
					String message = Application.SINGLE_OFFER_DUPLICATES_FILTERING+" "+
							Application.SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_WITH_HP_IDENTIFIED+" "+
							" new offer to add has higher target payout than already added offer: "+
							" existing offer: "+addedOffer.getTitle()+" payout in target currency: "+addedOffer.getPayoutInTargetCurrency()+" "+addedOffer.getAdProviderCodeName()+
							" new offer to add: "+newOfferToAdd.getTitle()+" payout in target currency: "+newOfferToAdd.getPayoutInTargetCurrency()+" "+newOfferToAdd.getAdProviderCodeName(); 
					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
							offerWall.getRealm().getId(), 
							LogStatus.WARNING, 
							message);

					return true;
				}
			}
		}
		
		return false;
	}

	public void markExistingOfferForRejection(Offer newOfferToAdd, OfferWallEntity offerWall) {
		for(int i=0;i<listAddedOffers.size();i++) {
			Offer addedOffer = listAddedOffers.get(i);
			if(addedOffer.getTitle().equals(newOfferToAdd.getTitle())) {
				if(addedOffer.getPayoutInTargetCurrency() < newOfferToAdd.getPayoutInTargetCurrency()) {

					String message = Application.SINGLE_OFFER_DUPLICATES_FILTERING+" "+
							Application.SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_MARKED_FOR_REJECTION+" "+
							" marking following offer as a duplicate for rejection: "+
							" offer title: "+addedOffer.getTitle()+" payout in target currency: "+addedOffer.getPayoutInTargetCurrency()+" "+addedOffer.getAdProviderCodeName(); 
					Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
							offerWall.getRealm().getId(), 
							LogStatus.WARNING, 
							message);
					
					listDuplicateOffersToReject.add(addedOffer); //mark existing offer for rejection
				}
			}
		}
	}

	public void rejectDuplicateOffersMarkedForRemoval(OfferWallEntity offerWall, ArrayList<IndividualOfferWall> listIndividualOfferWalls){
		for(int i=0;i<listDuplicateOffersToReject.size();i++) {
			Offer offerToReject = listDuplicateOffersToReject.get(i);
			for(int j=0;j<listIndividualOfferWalls.size();j++) {
				IndividualOfferWall individualOfferWall = listIndividualOfferWalls.get(j);
				ArrayList<Offer> listOfferWallOffers = individualOfferWall.getOffers();
				for(int k=listOfferWallOffers.size()-1;k>=0;k--) {
					Offer validatedOffer = listOfferWallOffers.get(k);
					if(validatedOffer.getTitle()!=null && validatedOffer.getTitle().equals(offerToReject.getTitle()) &&
							validatedOffer.getAdProviderCodeName().toString().equals(offerToReject.getAdProviderCodeName().toString()) &&
								validatedOffer.getPayoutInTargetCurrency()==offerToReject.getPayoutInTargetCurrency()) {
						listOfferWallOffers.remove(k);
						
						String message = Application.SINGLE_OFFER_DUPLICATES_FILTERING+" "+
								Application.SINGLE_OFFER_DUPLICATES_FILTERING_DUPLICATE_REJECTED+" "+
								" rejected duplicate offer due to lower payout: "+
								" offer title: "+validatedOffer.getTitle()+" payout in target currency: "+validatedOffer.getPayoutInTargetCurrency()+" "+validatedOffer.getAdProviderCodeName(); 
						Application.getElasticSearchLogger().indexLog(Application.OFFER_WALL_GENERATION_ACTIVITY, 
								offerWall.getRealm().getId(), 
								LogStatus.WARNING, 
								message);
					}
				}
			}
		}
	}
	
	
}
