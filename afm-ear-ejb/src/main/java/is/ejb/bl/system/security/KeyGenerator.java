package is.ejb.bl.system.security;

import javax.ejb.Stateless;

import org.apache.commons.lang.RandomStringUtils;

@Stateless
public class KeyGenerator {

	public static String generateKey(int length) {
		boolean letters = true;
		boolean numbers = true;
		String key = RandomStringUtils.random(length, letters, numbers);
		return key;
	}

	public static String generateKey(int length, boolean letters, boolean numbers) {
		String key = RandomStringUtils.random(length, letters, numbers);
		return key;
	}

}
