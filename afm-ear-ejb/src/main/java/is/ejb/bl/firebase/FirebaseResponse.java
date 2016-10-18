package is.ejb.bl.firebase;

public class FirebaseResponse {
	private int code;
	private String content;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public FirebaseResponse(int code, String content) {
		super();
		this.code = code;
		this.content = content;
	}
	@Override
	public String toString() {
		return "FirebaseResponse [code=" + code + ", content=" + content + "]";
	}
	
}
