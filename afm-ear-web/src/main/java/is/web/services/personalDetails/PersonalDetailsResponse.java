package is.web.services.personalDetails;

import is.ejb.dl.entities.PersonalDetailsEntity;
import is.web.services.APIResponse;

public class PersonalDetailsResponse extends APIResponse {
	private PersonalDetailsEntity personalDetails;

	public PersonalDetailsEntity getPersonalDetails() {
		return personalDetails;
	}

	public void setPersonalDetails(PersonalDetailsEntity personalDetails) {
		this.personalDetails = personalDetails;
	}



	
}
