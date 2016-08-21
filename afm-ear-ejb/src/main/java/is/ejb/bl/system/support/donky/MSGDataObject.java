package is.ejb.bl.system.support.donky;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.Expose;

public class MSGDataObject {
 
	@Expose
	private String RecipientExternalUserId;
	@Expose
	private String SenderDisplayName; 
	@Expose
	private String SenderNumber;
	@Expose
	private String SenderExternalUserId;
	@Expose
	private Object SenderAvatarUrl;
	@Expose
	private String MessageId;
	@Expose
	private String SenderMessageId;
	@Expose
	private String ConversationId;
	@Expose
	private String MessageType;
	@Expose
	private String Body;
	@Expose
	private List<Asset> Assets = new ArrayList<Asset>();
	@Expose
	private String SentTimestamp;
	@Expose
	private String ExternalRef;
	@Expose
	private String Scope;

	/**
	 * 
	 * @return The RecipientExternalUserId
	 */
	public String getRecipientExternalUserId() {
		return RecipientExternalUserId;
	}

	/**
	 * 
	 * @param RecipientExternalUserId
	 *            The RecipientExternalUserId
	 */
	public void setRecipientExternalUserId(String RecipientExternalUserId) {
		this.RecipientExternalUserId = RecipientExternalUserId;
	}

	/**
	 * 
	 * @return The SenderDisplayName
	 */
	public String getSenderDisplayName() {
		return SenderDisplayName;
	}

	/**
	 * 
	 * @param SenderDisplayName
	 *            The SenderDisplayName
	 */
	public void setSenderDisplayName(String SenderDisplayName) {
		this.SenderDisplayName = SenderDisplayName;
	}

	/**
	 * 
	 * @return The SenderNumber
	 */
	public String getSenderNumber() {
		return SenderNumber;
	}

	/**
	 * 
	 * @param SenderNumber
	 *            The SenderNumber
	 */
	public void setSenderNumber(String SenderNumber) {
		this.SenderNumber = SenderNumber;
	}

	/**
	 * 
	 * @return The SenderExternalUserId
	 */
	public String getSenderExternalUserId() {
		return SenderExternalUserId;
	}

	/**
	 * 
	 * @param SenderExternalUserId
	 *            The SenderExternalUserId
	 */
	public void setSenderExternalUserId(String SenderExternalUserId) {
		this.SenderExternalUserId = SenderExternalUserId;
	}

	/**
	 * 
	 * @return The SenderAvatarUrl
	 */
	public Object getSenderAvatarUrl() {
		return SenderAvatarUrl;
	}

	/**
	 * 
	 * @param SenderAvatarUrl
	 *            The SenderAvatarUrl
	 */
	public void setSenderAvatarUrl(Object SenderAvatarUrl) {
		this.SenderAvatarUrl = SenderAvatarUrl;
	}

	/**
	 * 
	 * @return The MessageId
	 */
	public String getMessageId() {
		return MessageId;
	}

	/**
	 * 
	 * @param MessageId
	 *            The MessageId
	 */
	public void setMessageId(String MessageId) {
		this.MessageId = MessageId;
	}

	/**
	 * 
	 * @return The SenderMessageId
	 */
	public String getSenderMessageId() {
		return SenderMessageId;
	}

	/**
	 * 
	 * @param SenderMessageId
	 *            The SenderMessageId
	 */
	public void setSenderMessageId(String SenderMessageId) {
		this.SenderMessageId = SenderMessageId;
	}

	/**
	 * 
	 * @return The ConversationId
	 */
	public String getConversationId() {
		return ConversationId;
	}

	/**
	 * 
	 * @param ConversationId
	 *            The ConversationId
	 */
	public void setConversationId(String ConversationId) {
		this.ConversationId = ConversationId;
	}

	/**
	 * 
	 * @return The MessageType
	 */
	public String getMessageType() {
		return MessageType;
	}

	/**
	 * 
	 * @param MessageType
	 *            The MessageType
	 */
	public void setMessageType(String MessageType) {
		this.MessageType = MessageType;
	}

	/**
	 * 
	 * @return The Body
	 */
	public String getBody() {
		return Body;
	}

	/**
	 * 
	 * @param Body
	 *            The Body
	 */
	public void setBody(String Body) {
		this.Body = Body;
	}



	public List<Asset> getAssets() {
		return Assets;
	}

	public void setAssets(List<Asset> assets) {
		Assets = assets;
	}

	/**
	 * 
	 * @return The SentTimestamp
	 */
	public String getSentTimestamp() {
		return SentTimestamp;
	}

	/**
	 * 
	 * @param SentTimestamp
	 *            The SentTimestamp
	 */
	public void setSentTimestamp(String SentTimestamp) {
		this.SentTimestamp = SentTimestamp;
	}

	/**
	 * 
	 * @return The ExternalRef
	 */
	public String getExternalRef() {
		return ExternalRef;
	}

	/**
	 * 
	 * @param ExternalRef
	 *            The ExternalRef
	 */
	public void setExternalRef(String ExternalRef) {
		this.ExternalRef = ExternalRef;
	}

	/**
	 * 
	 * @return The Scope
	 */
	public String getScope() {
		return Scope;
	}

	/**
	 * 
	 * @param Scope
	 *            The Scope
	 */
	public void setScope(String Scope) {
		this.Scope = Scope;
	}

	@Override
	public String toString() {
		return "MSGDataObject [RecipientExternalUserId="
				+ RecipientExternalUserId + ", SenderDisplayName="
				+ SenderDisplayName + ", SenderNumber=" + SenderNumber
				+ ", SenderExternalUserId=" + SenderExternalUserId
				+ ", SenderAvatarUrl=" + SenderAvatarUrl + ", MessageId="
				+ MessageId + ", SenderMessageId=" + SenderMessageId
				+ ", ConversationId=" + ConversationId + ", MessageType="
				+ MessageType + ", Body=" + Body + ", Assets=" + Assets
				+ ", SentTimestamp=" + SentTimestamp + ", ExternalRef="
				+ ExternalRef + ", Scope=" + Scope + "]";
	}

	

}
