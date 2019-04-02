package org.openforis.collect.manager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public abstract class MD5PasswordEncoder {
	
	private static final String MD5 = "MD5";

	public static String encode(CharSequence password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(MD5);
			byte[] digest = messageDigest.digest(password.toString().getBytes());
			char[] resultChar = Hex.encodeHex(digest);
			return new String(resultChar);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error encoding user password", e);
		}
	}

	public static boolean matches(CharSequence rawPassword, String encodedPassword) {
		return encode(rawPassword).equals(encodedPassword);
	}

}
