package is.ejb.bl.denominationModels;

public class DenominationModelRow implements java.io.Serializable {

	private String name=""; //just to let this row be selectable model on the web side
	
	private double sourceOfferPayoffValue=0;

	private double targetOfferPayoffValue=0;

	private double revenueSpit = 0; //value based on which revenue is split
	private double airtimePayoff = 0; //value that goes to airtime (user reward)
	
	public double getSourceOfferPayoffValue() {
		return sourceOfferPayoffValue;
	}
	public void setSourceOfferPayoffValue(double sourceOfferPayoffValue) {
		name = name+sourceOfferPayoffValue;
		this.sourceOfferPayoffValue = sourceOfferPayoffValue;
	}
	public double getTargetOfferPayoffValue() {
		return targetOfferPayoffValue;
	}
	public void setTargetOfferPayoffValue(double targetOfferPayoffValue) {
		name = name+targetOfferPayoffValue;
		this.targetOfferPayoffValue = targetOfferPayoffValue;
	}
	public double getRevenueSpit() {
		return revenueSpit;
	}
	public void setRevenueSpit(double revenueSpit) {
		name = name+revenueSpit;
		this.revenueSpit = revenueSpit;
	}
	public double getAirtimePayoff() {
		return airtimePayoff;
	}
	public void setAirtimePayoff(double airtimePayoff) {
		name = name+airtimePayoff;
		this.airtimePayoff = airtimePayoff;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
