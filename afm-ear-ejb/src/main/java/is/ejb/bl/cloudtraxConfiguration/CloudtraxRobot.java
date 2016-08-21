package is.ejb.bl.cloudtraxConfiguration;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;
public class CloudtraxRobot {
	public HtmlPage login(String username, String password)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {
		final WebClient webClient = new WebClient(
				BrowserVersion.INTERNET_EXPLORER_11);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);

		// Get the first page
		final HtmlPage loginPage = webClient
				.getPage("https://cloudtrax.com/dashboard.php");

		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final List<HtmlForm> formsList = loginPage.getForms();
		HtmlForm form = formsList.get(0);

		final HtmlSubmitInput button = form.getInputByName("edit");
		final HtmlTextInput loginInput = form.getInputByName("login");
		final HtmlPasswordInput passwordInput = form.getInputByName("login-pw");

		loginInput.setValueAttribute(username);
		passwordInput.setValueAttribute(password);

		final HtmlPage dashboardPage = button.click();

		try {

			HtmlAnchor link = dashboardPage.getAnchorByText("Edit Network");
			HtmlPage editPage = link.click();
			return editPage;
		} catch (ElementNotFoundException exc) {
			return null;
		}

	}

	public void setNetworkName(HtmlPage page, String networkName)
			throws ElementNotFoundException, IOException {
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("ESSID").setValueAttribute(networkName);
		form.getInputByName("submit2").click();
	}

	public String getNetworkName(HtmlPage page) {
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("ESSID").getValueAttribute();
		return value;
	}

	public void setNetworkPassword(HtmlPage page, String networkPassword)
			throws ElementNotFoundException, IOException {
		if (networkPassword.length() > 7) {
			HtmlForm form = page.getForms().get(0);
			form.getInputByName("AP1_KEY").setValueAttribute(networkPassword);
			form.getInputByName("submit2").click();
		}
	}

	public String getNetworkPassword(HtmlPage page) {
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("AP1_KEY").getValueAttribute();
		return value;
	}
	
	public boolean isChillispotEnabled(HtmlPage page)
	{
		HtmlForm form = page.getForms().get(0);
		List <HtmlInput> inputs = form.getInputsByValue("D");
		for (HtmlInput inp : inputs)
		{
			if (inp.getId().equals("RADIUS"))
				return inp.isChecked();
		}
	
		return false;
		
	}
	
	
	public void setChillispotEnblaed(HtmlPage page) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		List <HtmlInput> inputs = form.getInputsByValue("D");
		for (HtmlInput inp : inputs)
		{
			if (inp.getId().equals("RADIUS"))
				inp.setChecked(true);
		}
		form.getInputByName("submit2").click();
	}
	
	
	
	public String getRadiusServer1(HtmlPage page)
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_RADIUS1").getValueAttribute();
		return value;
	}
	
	public void setRadiusServer1(HtmlPage page,String server) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_RADIUS1").setValueAttribute(server);
		form.getInputByName("submit2").click();
	}
	
	
	public String getRadiusServer2(HtmlPage page)
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_RADIUS2").getValueAttribute();
		return value;
	}
	
	public void setRadiusServer2(HtmlPage page,String server) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_RADIUS2").setValueAttribute(server);
		form.getInputByName("submit2").click();
	}
	
	public void setRadiusSecret(HtmlPage page, String password) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_SECRET").setValueAttribute(password);
		form.getInputByName("submit2").click();
	}
	
	public String getRadiusSecret(HtmlPage page)
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_SECRET").getValueAttribute();
		return value;
	}
	
	public void setRadiusNASID(HtmlPage page,String nasid) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("NASID").setValueAttribute(nasid);
		form.getInputByName("submit2").click();
	}
	
	public String getRadiusNASID(HtmlPage page) 
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("NASID").getValueAttribute();
		return value;
	}
	
	public void setCaptivePortalServer(HtmlPage page,String server) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_UAMSERVER").setValueAttribute(server);
		form.getInputByName("submit2").click();
	}
	
	public String getCaptivePortalServer(HtmlPage page) 
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_UAMSERVER").getValueAttribute();
		return value;
	}
	
	
	public void setCaptivePortalURL(HtmlPage page,String server) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_UAMURL").setValueAttribute(server);
		form.getInputByName("submit2").click();
	}
	
	public String getCaptivePortalURL(HtmlPage page) 
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_UAMURL").getValueAttribute();
		return value;
	}
	
	
	public void setCaptivePortalSecret(HtmlPage page,String secret) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		form.getInputByName("CP_UAM_SECRET").setValueAttribute(secret);
		form.getInputByName("submit2").click();
	}
	
	public List<String> getCaptivePortalAllowedDomains(HtmlPage page) 
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("UAMDOMAINS").getValueAttribute();
		String [] values = value.split(",");
		return Arrays.asList(values);
	}
	
	
	public void setCaptivePortalAllowedDomains(HtmlPage page,List<String> allowedDomains) throws ElementNotFoundException, IOException
	{
		HtmlForm form = page.getForms().get(0);
		String allowedDomainsList = "";
		for (String domain: allowedDomains)
		{
			allowedDomainsList = allowedDomainsList + domain + ",";
		}
		form.getInputByName("UAMDOMAINS").setValueAttribute(allowedDomainsList);
		form.getInputByName("submit2").click();
	}
	
	public String getCaptivePortalSecret(HtmlPage page) 
	{
		HtmlForm form = page.getForms().get(0);
		String value = form.getInputByName("CP_UAM_SECRET").getValueAttribute();
		return value;
	}

	
	
	
	public List<String> getAcessPoints(String username,String password)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		List<String> accessPointsList = new ArrayList<String>();
		final WebClient webClient = new WebClient(
				BrowserVersion.INTERNET_EXPLORER_11);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);

		// Get the first page
		final HtmlPage loginPage = webClient
				.getPage("https://cloudtrax.com/dashboard.php");

		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final List<HtmlForm> formsList = loginPage.getForms();
		HtmlForm form = formsList.get(0);

		final HtmlSubmitInput button = form.getInputByName("edit");
		final HtmlTextInput loginInput = form.getInputByName("login");
		final HtmlPasswordInput passwordInput = form.getInputByName("login-pw");

		loginInput.setValueAttribute(username);
		passwordInput.setValueAttribute(password);

		final HtmlPage dashboardPage = button.click();
		// Get the first page
		final HtmlPage apPage = webClient
				.getPage("https://www.cloudtrax.com/public/index.php/network/nodelist?network="+username);
		
		//System.out.println(apPage.asText());
		
		Pattern pattern = Pattern.compile("((?:[0-9A-F][0-9A-F]:){5}(?:[0-9A-F][0-9A-F]))(?![:0-9A-F])");
		Matcher m = pattern.matcher(apPage.asText());
		
		while (m.find())
		{
			accessPointsList.add(m.group(0));
		}
		return accessPointsList;
		
	}
	
	
	
	
	public List<String> getAcessPointsToDO(String username,String password)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		List<String> accessPointsList = new ArrayList<String>();
		final WebClient webClient = new WebClient(
				BrowserVersion.INTERNET_EXPLORER_11);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);

		// Get the first page
		final HtmlPage loginPage = webClient
				.getPage("https://cloudtrax.com/dashboard.php");

		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final List<HtmlForm> formsList = loginPage.getForms();
		HtmlForm form = formsList.get(0);

		final HtmlSubmitInput button = form.getInputByName("edit");
		final HtmlTextInput loginInput = form.getInputByName("login");
		final HtmlPasswordInput passwordInput = form.getInputByName("login-pw");

		loginInput.setValueAttribute(username);
		passwordInput.setValueAttribute(password);

		final HtmlPage dashboardPage = button.click();
		// Get the first page
		final HtmlPage apPage = webClient
				.getPage("https://www.cloudtrax.com/public/index.php/network/nodelist?network="+username);
		HtmlForm htmlform = apPage.getForms().get(0);
		List<HtmlInput> inputList =htmlform.getInputsByName("col[]");
		for (HtmlInput input : inputList)
		{
			if (input.getValueAttribute().equals("2"))
			{
				System.out.println(input.getId());
				input.setChecked(true);
			}
		}
		
		HtmlInput but = htmlform.getInputByValue("Submit");
		System.out.println(but.getId());
		
		HtmlPage apRefreshPage = htmlform.getInputByValue("Submit").click();
		
		
		System.out.println(apRefreshPage.asText());
				
		return accessPointsList;
	}
	
}
