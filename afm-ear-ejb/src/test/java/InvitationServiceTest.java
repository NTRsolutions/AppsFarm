import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class InvitationServiceTest {

	private final String NETWORK_NAME = "BPM";
	private final String PATH = "http://127.0.0.1:8079/ab/svc/v1/";
	private final String ARGUMENT_SEPARATOR = "&";

	public static void main(String[] args) {
		new InvitationServiceTest();

	}

	private InvitationServiceTest() {
		String code = generateCode();
		String fullName = generateFullName();
		String emailInviting = generateEmail();
		String emailInvited = generateEmail();
		String phoneNumberInviting = generatePhoneNumber();
		String phoneNumberInvited = generatePhoneNumber();
		String password = generatePassword();

		// INVITATION
		String invitationAddress = PATH + "invite?" + "code=" + code
				+ ARGUMENT_SEPARATOR + "emailInvited=" + emailInvited
				+ ARGUMENT_SEPARATOR + "emailInviting=" + emailInviting
				+ ARGUMENT_SEPARATOR + "phoneNumberInviting="
				+ phoneNumberInviting;

		connectAndShowReply(invitationAddress);

		// REGISTRATION
		String registrationAddress = PATH + "registerUser?" + "fullName="
				+ fullName + ARGUMENT_SEPARATOR + "email=" + emailInvited
				+ ARGUMENT_SEPARATOR + "phoneNumber=" + phoneNumberInvited
				+ ARGUMENT_SEPARATOR + "password=" + password
				+ ARGUMENT_SEPARATOR + "networkName=" + NETWORK_NAME
				+ ARGUMENT_SEPARATOR + "code=" + code;
		
		connectAndShowReply(registrationAddress);
	}

	private void connectAndShowReply(String address) {
		try {
			URL url = new URL(address);
			URLConnection connection = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String generateFullName() {
		Random rand = new Random();
		int number = rand.nextInt(900) + 100;

		String name = "User" + number;

		return name;
	}

	private String generateEmail() {
		Random rand = new Random();
		String[] letters = { "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
				"a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c",
				"v", "b", "n", "m" };
		String name = "";
		int lengthOfName = rand.nextInt(4) + 3;
		for (int i = lengthOfName; i > 0; i--) {
			name += letters[rand.nextInt(letters.length)];
		}

		String email = name + "@asd.com";
		return email;
	}

	private String generatePhoneNumber() {
		Random rand = new Random();
		String phoneNumber = "";

		for (int i = 0; i < 9; i++) {
			phoneNumber += rand.nextInt(3) + 5;
		}

		return phoneNumber;
	}

	private String generatePassword() {
		Random rand = new Random();
		String[] characters = { "q", "w", "e", "r", "t", "y", "u", "i", "o",
				"p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x",
				"c", "v", "b", "n", "m", "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9" };
		String password = "";
		int lengthOfPassword = rand.nextInt(4) + 8;
		for (int i = lengthOfPassword; i > 0; i--) {
			password += characters[rand.nextInt(characters.length)];
		}

		return password;
	}

	private String generateCode() {
		Random rand = new Random();
		String code = "";

		for (int i = 0; i < 6; i++) {
			code += rand.nextInt(10);
		}

		return code;
	}

}
