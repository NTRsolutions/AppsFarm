package is.web.services.application;

import is.web.services.APIResponse;

public class ApplicationConfigurationResponse extends APIResponse {
	private ApplicationConfiguration applicationConfiguration;

	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

}
