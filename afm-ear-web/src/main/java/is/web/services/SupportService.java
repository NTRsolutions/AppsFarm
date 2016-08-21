package is.web.services;

import is.ejb.bl.business.Application;
import is.ejb.bl.business.RespCodesEnum;
import is.ejb.bl.business.RespStatusEnum;
import is.ejb.bl.system.logging.LogStatus;
import is.ejb.bl.system.support.ZendeskManager;
import is.ejb.bl.system.support.donky.Asset;
import is.ejb.bl.system.support.donky.DonkyForwardRequest;
import is.ejb.bl.system.support.donky.MSGDataObject;
import is.ejb.bl.system.support.donky.ZendeskResponse;
import is.ejb.dl.dao.DAOAppUser;
import is.ejb.dl.dao.DAODonkySupport;
import is.ejb.dl.dao.DAORealm;
import is.ejb.dl.dao.DAORewardType;
import is.ejb.dl.entities.AppUserEntity;
import is.ejb.dl.entities.DonkySupportEntity;
import is.ejb.dl.entities.RealmEntity;
import is.ejb.dl.entities.RewardTypeEntity;

import java.net.URL;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.zendesk.client.v2.model.Ticket;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/")
public class SupportService {

	@Inject
	private Logger logger;

	@Inject
	private DAODonkySupport daoDonkySupport;

	@Inject
	private DAORealm daoRealm;

	@Inject
	private DAOAppUser daoAppUser;

	@Context
	private HttpServletRequest httpRequest;

	@Inject
	private ZendeskManager zendeskManager;

	@Inject
	private DAORewardType daoRewardType;

	private final String CINETREATS_API_KEY = "sUeUt4uFhR156ziOXZe0F8HwY80ZoNQsNR7fFVFqfbtcrklL3NJrpmppqG1g3H4KHCynzhdiNqUs8Ikf6KkBIA";
	private final String GOAHEAD_API_KEY = "4f603F1fxZOdM+HiYOr0lgcJWgT5KJI4ctdRjuA5L3zY4cY9yjxeye58Sd0DocTfhUfUtGF21KqanWlKJJNDw";
	private final String AIRREWARDZ_API_KEY = "lOYyOYjTwK234J7w0nIkiGQyuuspPg95eHpvsx6+GJbfcMbMTSII3AtCzkSUnvkl9++lFv+CLgJ2dodMSsuQ";
	private final String CINETREATS_SPACE_ID = "95ca9cab-c181-4e2b-ad59-bf5601ea557c";
	private final String GOAHEAD_SPACE_ID = "014eee1c-6abc-4d69-a5ec-3509873ec269";
	private final String AIRREWARDZ_SPACE_ID = "";

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/v1/triggerDonkeyChatSupportTicketRequest/")
	public Response triggerDonkeyChatSupportTicketRequest(final DonkyForwardRequest input) {

		try {
			logger.info("Received message from donky networks:");

			// if input isnt empty
			if (input != null)
				// if request is "MSG" Type
				if (input.getNotifications()[0].getType().equals("MSG")) {
					logger.info(input.toString());
					
					// System.out.println(input.toString());

					Gson gson = new Gson();
					MSGDataObject msgDataObject = gson.fromJson(input.getNotifications()[0].getData(),
							MSGDataObject.class);
					if (msgDataObject.getRecipientExternalUserId().equals("24aula") || 
							msgDataObject.getRecipientExternalUserId().equals("7889110554")){
						logger.info("Recipent is support");
					}else{
						logger.info("Recipent is not support. Returning success and not processing");
						return Response.ok().build();
					}

					String dataContent = input.toString() + msgDataObject.toString();
					System.out.println(dataContent);
					Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1, LogStatus.OK,
							Application.DONKEY_REQUEST + " received request: " + " username: "
									+ msgDataObject.getSenderDisplayName() + " phoneNumber: "
									+ msgDataObject.getSenderExternalUserId() + " message: " + msgDataObject.getBody()
									+ " recipient: " + msgDataObject.getRecipientExternalUserId());

					boolean isAlredyTicketed = false;

					DonkySupportEntity donkySupportEntity = null;
					donkySupportEntity = daoDonkySupport.findByConversationId(msgDataObject.getConversationId());
					if (donkySupportEntity == null)
						isAlredyTicketed = false;
					else
						isAlredyTicketed = true;

					String ticketMessage = null;
					if (msgDataObject.getAssets().size() == 0)
						ticketMessage = msgDataObject.getBody();
					else {
						ticketMessage = msgDataObject.getBody() + "\n\n\nAttachments("
								+ msgDataObject.getAssets().size() + " total):";
						for (Asset asset : msgDataObject.getAssets()) {
							ticketMessage = ticketMessage + "\n(" + asset.getMimeType() + ")" + " "
									+ asset.getAssetUrl();
						}
					}

					String applicationName = null;
					if (input.getApplicationSpaceId().equals(GOAHEAD_SPACE_ID))
						applicationName = "GoAhead";
					if (input.getApplicationSpaceId().equals(CINETREATS_SPACE_ID))
						applicationName = "Cinetreats";

					if (isAlredyTicketed == false) {
						logger.info("Creating ticket...");
						donkySupportEntity = new DonkySupportEntity();
						AppUserEntity appUser = getAppUserWithPhoneNumber(msgDataObject.getSenderExternalUserId());
						

						donkySupportEntity.setExternalUserId(msgDataObject.getSenderExternalUserId());
						donkySupportEntity.setConversationId(msgDataObject.getConversationId());
						donkySupportEntity.setCreationTime(new Timestamp(new Date().getTime()));
						donkySupportEntity.setUserId("" + appUser.getId());
						donkySupportEntity = daoDonkySupport.createOrUpdate(donkySupportEntity);
						donkySupportEntity.setRewardType(appUser.getRewardTypeName());

						RealmEntity realm = daoRealm.findById(appUser.getRealmId());

						Ticket ticket = zendeskManager.createTicket(msgDataObject.getSenderDisplayName(),
								appUser.getEmail(), "User support question (chatz)", ticketMessage,
								realm.getSupportSystemUrl(), realm.getSupportSystemUserName(),
								realm.getSupportSystemPassword(), applicationName);
						List<String> tags = new ArrayList<String>();
						tags.add("chatz");
						zendeskManager.setTicketTags(ticket, tags, realm.getSupportSystemUrl(),
								realm.getSupportSystemUserName(), realm.getSupportSystemPassword(), applicationName);
						logger.info("Ticket created:" + ticket + " " + ticket.getId());

						donkySupportEntity.setTicketId(String.valueOf(ticket.getId()));
						donkySupportEntity = daoDonkySupport.createOrUpdate(donkySupportEntity);
						triggerDonkeyChatInititalMessage(donkySupportEntity);

						Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1,
								LogStatus.OK,
								Application.ZENDESK_REQUEST + " inserted new ticket: " + donkySupportEntity);

					} else {
						// if there is already ticket
						logger.info("Ticket is present, can add comment.");
						AppUserEntity appUser = daoAppUser.findByPhoneNumber(msgDataObject.getSenderExternalUserId());
						RealmEntity realm = daoRealm.findById(appUser.getRealmId());

						if (donkySupportEntity.getTicketId() != null) {

							String status = zendeskManager.getTicketStatus(
									Integer.parseInt(donkySupportEntity.getTicketId()), realm.getSupportSystemUrl(),
									realm.getSupportSystemUserName(), realm.getSupportSystemPassword(),
									applicationName);
							if (status.toLowerCase().equals("solved")) {
								logger.info("Ticket is solved, cant add comment");
								Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1,
										LogStatus.OK,
										Application.ZENDESK_REQUEST
												+ " cant insert comment to zendesk due to closed ticket "
												+ donkySupportEntity + "comment: " + msgDataObject.getBody() + " from: "
												+ msgDataObject.getSenderDisplayName());
							} else {
								logger.info("Adding comment to ticket.");
								zendeskManager.AddCommentToTicket(Integer.parseInt(donkySupportEntity.getTicketId()),
										msgDataObject.getSenderDisplayName(), ticketMessage,
										realm.getSupportSystemUrl(), realm.getSupportSystemUserName(),
										realm.getSupportSystemPassword(), applicationName);

								Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1,
										LogStatus.OK,
										Application.ZENDESK_REQUEST + " inserted new comment to existing ticket: "
												+ donkySupportEntity + "comment: " + msgDataObject.getBody() + " from: "
												+ msgDataObject.getSenderDisplayName());
							}
						} else {
							logger.info("Ticket id is empty.");
						}
					}
				}
		} catch (Exception exc) {

			exc.printStackTrace();
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1, LogStatus.ERROR,
					Application.DONKEY_REQUEST + " error: " + exc.toString());
		}

		return Response.ok().build();
	}

	private AppUserEntity getAppUserWithPhoneNumber(String phoneNumber) {
		AppUserEntity appUser = null;
		try {
			if (phoneNumber != null) {
				appUser = daoAppUser.findByPhoneNumber(phoneNumber);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return appUser;
	}

	private void sendMessageToDonkyNetwork(DonkySupportEntity donkySupportEntity, ZendeskResponse response) {
		logger.info("Sending message to Donky Network");

		try {
			String url = "https://integrator-api.mobiledonky.com/api/content/send";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("ApiKey", AIRREWARDZ_API_KEY);

			String responseFromAccount = response.getFromAccount();
			if (responseFromAccount != null && responseFromAccount.toLowerCase().contains("goahead")) {
				logger.info("Changing donky network apikey to goahead");
				con.setRequestProperty("ApiKey", GOAHEAD_API_KEY);
			}

			if (responseFromAccount != null && responseFromAccount.toLowerCase().toLowerCase().contains("cine")) {
				logger.info("Changing donky network apikey to cinetreats");
				con.setRequestProperty("ApiKey", CINETREATS_API_KEY);

			}

			if (response.getMessageFormatted() != null
					&& response.getMessageFormatted().toLowerCase().contains("attach")) {
				String[] parts = response.getMessageFormatted().split("\\s+");
				List<String> urls = new ArrayList<String>();
				for (String part : parts) {

					urls.add(part);

				}

				if (urls.size() > 0) {
					response.setMessage(response.getMessage() + "<br/>Attachments:");
					for (String strUrl : urls) {
						response.setMessage(response.getMessage() + "<br/><br/>" + strUrl);
					}
				}
			}

			if (response.getStatus() != null && response.getStatus().toLowerCase().equals("solved")) {
				response.setMessage(response.getMessage()
						+ "<br/><br/>-----------------<br/>This ticket is solved.If you want contact support please start a new chat again.");
			}
			String urlParameters = "[{\"audience\": {\"type\": \"SpecifiedUsers\",\"users\": [{\"userId\": \""
					+ donkySupportEntity.getExternalUserId()
					+ "\"}]},\"content\": {\"type\": \"ChatMessage\",\"message\": {\"body\": \"" + response.getMessage()
					+ "" + "\",\"conversationId\":\"" + donkySupportEntity.getConversationId().replaceAll("\\s+", "")
					+ "\",\"canReply\": true}}}]";

			logger.info("Parameters sent to donky: " + urlParameters);

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			InputStream inputStream;
			logger.info("Donky Network api call result code: " + con.getResponseCode());
			if (con.getResponseCode() == 200) {
				inputStream = con.getInputStream();
			} else {

				inputStream = con.getErrorStream();
			}

			if (inputStream != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
				String inputLine;
				StringBuffer resultResponse = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					resultResponse.append(inputLine);
				}
				in.close();

				logger.info("Donky Network api call result response: " + resultResponse);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private void triggerDonkeyChatInititalMessage(DonkySupportEntity donkySupportEntity) {

		try {
			logger.info("Sending donky support initial auto respond message");
			ZendeskResponse response = new ZendeskResponse();
			String message = daoRealm.findByName("BPM").getDonkyAutoRespondMessage();
			response.setMessage(message);
			sendMessageToDonkyNetwork(donkySupportEntity, response);

		} catch (Exception exc) {
			logger.info(exc.getMessage());
		}

	}

	private List<RewardTypeEntity> getRewardTypeWithPartName(String partName) {
		List<RewardTypeEntity> foundRewardTypes = new ArrayList<RewardTypeEntity>();
		try {
			List<RewardTypeEntity> rewardTypes = daoRewardType.findAll();
			for (RewardTypeEntity rewardType : rewardTypes) {
				if (rewardType.getName().toLowerCase().contains(partName.toLowerCase())) {
					foundRewardTypes.add(rewardType);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return foundRewardTypes;
	}

	@GET
	@Produces("application/json")
	@Path("/v1/triggerDonkeyChatSupportTicketResponse/")
	public void triggerDonkeyChatSupportTicketResponse(@QueryParam("result") String result) {
		try {

			if (result != null) {
				ZendeskResponse response = new Gson().fromJson(result, ZendeskResponse.class);
				logger.info("Received service call from zendesk. Call parameters:" + result.toString());
				List<RewardTypeEntity> rewardTypesToCheck = null;
				String fromAccount = response.getFromAccount().toLowerCase();
				if (fromAccount.contains("cine")) {
					rewardTypesToCheck = getRewardTypeWithPartName("cine");
				} else if (fromAccount.contains("trippa") || fromAccount.contains("goahead")) {
					rewardTypesToCheck = getRewardTypeWithPartName("trippa");
				} else if (fromAccount.contains("air")) {
					rewardTypesToCheck = getRewardTypeWithPartName("air");
				} else {
					rewardTypesToCheck = new ArrayList<RewardTypeEntity>();
				}
				DonkySupportEntity donkySupportEntity = null;
				logger.info("Looking for ticket with id : " + response.getTicketId() + " in "
						+ rewardTypesToCheck.size() + " reward types.");
				for (RewardTypeEntity rewardType : rewardTypesToCheck) {
					donkySupportEntity = daoDonkySupport
							.findByTicketIdAndRewardType(String.valueOf(response.getTicketId()), rewardType.getName());
					if (donkySupportEntity != null) {
						break;
					}
				}

				if (donkySupportEntity != null) {
					logger.info("Found support entity for zendesk service call.");
					this.sendMessageToDonkyNetwork(donkySupportEntity, response);
				} else {
					logger.info("Didnt found support entity for zendesk service call.");
				}
			}
		} catch (Exception exc) {
			logger.severe(exc.toString());
			Application.getElasticSearchLogger().indexLog(Application.SUPPORT_TICKET_ACTIVITY, -1, LogStatus.ERROR,
					Application.DONKEY_RESPONSE + " error: " + exc.toString());
		}

	}

	@GET
	@Produces("application/json")
	@Path("/v1/saveClickUrl/")
	public String saveClickUrl(@QueryParam("phoneNumber") String phoneNumber,
			@QueryParam("phoneNumberExt") String phoneNumberExt, @QueryParam("offerId") String offerId,
			@QueryParam("url") String url) {

		Application.getElasticSearchLogger().indexLog(Application.CLICK_URL_ACTIVITY, -1, LogStatus.OK,
				Application.CLICK_URL_ACTIVITY + " " + " phoneNumber: " + phoneNumber + " phoneNumberExt: "
						+ phoneNumberExt + " offerId: " + offerId + " url: " + url);

		return "{\"status\":\"" + RespStatusEnum.SUCCESS + "\", " + "\"code\":\"" + RespCodesEnum.OK_NO_CONTENT + "\"}";
	}

}
