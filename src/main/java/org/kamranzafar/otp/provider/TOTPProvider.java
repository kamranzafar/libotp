/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package org.kamranzafar.otp.provider;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.kamranzafar.otp.OTPProviderBase;

/**
 * @author Kamran Zafar
 * 
 *         RFC 6238 Time based OTP
 */
public class TOTPProvider extends OTPProviderBase {

	/**
	 * HmacSHA1, HmacSHA256, HmacSHA512
	 */
	private static final String ALGORITHM = "algorithm";
	private static final String DEFAULT_ALGORITHM = "HmacSHA1";

	public String getName() {
		return "TOTP";
	}

	public String generate(String key, String base, int digits) {
		String alg = DEFAULT_ALGORITHM;

		if (properties.containsKey(ALGORITHM)) {
			alg = properties.get(ALGORITHM).toString();
		}

		try {
			return generateTOTP("" + key, base, "" + digits, alg);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * This method uses the JCE to provide the crypto algorithm. HMAC computes a
	 * Hashed Message Authentication Code with the crypto hash algorithm as a
	 * parameter.
	 * 
	 * @param crypto
	 *            : the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
	 * @param keyBytes
	 *            : the bytes to use for the HMAC key
	 * @param text
	 *            : the message or text to be authenticated
	 */
	private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
		try {
			Mac hmac;
			hmac = Mac.getInstance(crypto);
			SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
			hmac.init(macKey);
			return hmac.doFinal(text);
		} catch (GeneralSecurityException gse) {
			throw new UndeclaredThrowableException(gse);
		}
	}

	/**
	 * This method converts a HEX string to Byte[]
	 * 
	 * @param hex
	 *            : the HEX string
	 * 
	 * @return: a byte array
	 */

	private static byte[] hexStr2Bytes(String hex) {
		// Adding one byte to get the right conversion
		// Values starting with "0" can be converted
		byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

		// Copy all the REAL bytes, not the "first"
		byte[] ret = new byte[bArray.length - 1];
		for (int i = 0; i < ret.length; i++)
			ret[i] = bArray[i + 1];
		return ret;
	}

	/**
	 * This method generates a TOTP value for the given set of parameters.
	 * 
	 * @param key
	 *            : the shared secret, HEX encoded
	 * @param time
	 *            : a value that reflects a time
	 * @param returnDigits
	 *            : number of digits to return
	 * @param crypto
	 *            : the crypto function to use
	 * 
	 * @return: a numeric String in base 10 that includes
	 *          {@link truncationDigits} digits
	 */

	public static String generateTOTP(String key, String time, String returnDigits, String crypto) {
		int codeDigits = Integer.decode(returnDigits).intValue();
		String result = null;

		// Using the counter
		// First 8 bytes are for the movingFactor
		// Compliant with base RFC 4226 (HOTP)
		while (time.length() < 16)
			time = "0" + time;

		// Get the HEX in a Byte[]
		byte[] msg = hexStr2Bytes(time);
		byte[] k = hexStr2Bytes(key);

		byte[] hash = hmac_sha(crypto, k, msg);

		// put selected bytes into result int
		int offset = hash[hash.length - 1] & 0xf;

		int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
				| ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

		int otp = binary % ((int) Math.pow(10, codeDigits));

		result = Integer.toString(otp);
		while (result.length() < codeDigits) {
			result = "0" + result;
		}
		return result;
	}
}
