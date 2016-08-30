package is.ejb.bl.email;

import java.io.Serializable;

public class EmailHolder implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8130741004169536434L;
	private String recipent;
	private String title;
	private String content;
	public String getRecipent() {
		return recipent;
	}
	public void setRecipent(String recipent) {
		this.recipent = recipent;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "EmailHolder [recipent=" + recipent + ", title=" + title + ", content=" + content + "]";
	}
	
}
