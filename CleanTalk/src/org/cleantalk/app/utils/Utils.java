package org.cleantalk.app.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.cleantalk.app.model.Request;
import org.cleantalk.app.model.Site;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.provider.Settings.Secure;

public class Utils {

	public static String getDeviceId(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	/**
	 * Decrypt value with some key and initialization vector used AES128 Decrypting algorithm. Encrypted value encode to BASE64 format.
	 * 
	 * @param value
	 *            - source value
	 * @param key
	 *            - encryption key
	 * @param initialisationVector
	 *            - initialization vector
	 * @return encrypted and encoded value
	 */
	public static String AES128Decrypt(String value, String key, String initialisationVector) {
		if (value == null) {
			return null;
		}
		try {
			byte[] keyValue = key.getBytes();
			byte[] initVector = initialisationVector.getBytes();
			keyValue = (byte[]) resizeArray(keyValue, 16);
			initVector = (byte[]) resizeArray(initVector, 16);

			SecretKeySpec secretKey = new SecretKeySpec(keyValue, "AES");

			byte[] out = null;

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec ips = new IvParameterSpec(initVector);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ips);
			out = cipher.doFinal(Base64.decode(value.getBytes()));
			return new String(out);

		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Encrypt value with some key and initialization vector used AES128 Encrypting algorithm. Encrypted value encode to BASE64 format.
	 * 
	 * @param value
	 *            - source value
	 * @param key
	 *            - encryption key
	 * @param initialisationVector
	 *            - initialization vector
	 * @return encrypted and encoded value
	 */
	public static String AES128Encrypt(String value, String key, String initialisationVector) {
		if (value == null) {
			return null;
		}
		try {
			byte[] keyValue = key.getBytes();
			byte[] initVector = initialisationVector.getBytes();

			keyValue = (byte[]) resizeArray(keyValue, 16);
			initVector = (byte[]) resizeArray(initVector, 16);
			SecretKeySpec secretKey = new SecretKeySpec(keyValue, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec ips = new IvParameterSpec(initVector);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ips);
			byte[] ciphertext = cipher.doFinal(value.getBytes());
			return Base64.encodeToString(ciphertext, false);

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (IllegalBlockSizeException e4) {
			e4.printStackTrace();
		} catch (BadPaddingException e5) {
			e5.printStackTrace();
		} catch (InvalidAlgorithmParameterException e6) {
			e6.printStackTrace();
		}
		return null;
	}

	/**
	 * Reallocates an array with a new size, and copies the contents of the old array to the new array.
	 * 
	 * @param oldArray
	 *            the old array, to be reallocated.
	 * @param newSize
	 *            the new array size.
	 * @return A new array with the same contents.
	 */
	private static Object resizeArray(Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) {
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		}
		return newArray;
	}

	public static long getWeekAgoTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -7);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis() / 1000L;
	}

	public static long getTodayTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis() / 1000L;
	}

	public static long getYesterdayTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis() / 1000L;
	}
	
	public static List<Site> parseSites(JSONArray array) {
		List<Site> result = new ArrayList<Site>();
		int len = array.length();

		for (int i = 0; i < len; i++) {
			JSONObject obj = null;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Site site = null;
			try {
				site = new Site(
						obj.getString("servicename"),
						obj.getString("service_id"),
						obj.getString("favicon_url"),
						obj.getJSONObject("today").getInt("spam"),
						obj.getJSONObject("today").getInt("allow"),
						obj.getJSONObject("yesterday").getInt("spam"),
						obj.getJSONObject("yesterday").getInt("allow"),
						obj.getJSONObject("week").getInt("spam"), 
						obj.getJSONObject("week").getInt("allow"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result.add(site);

		}
		return result;
	}
	
	public static List<Request> parseRequests(JSONArray array) {
		List<Request> result = new ArrayList<Request>();
		int len = array.length();

		for (int i = 0; i < len; i++) {
			JSONObject obj = null;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Request site = null;
			try {
				site = new Request(
						obj.getString("request_id"),
						obj.getInt("allow")==1,
						obj.getString("datetime"),
						obj.getString("sender_email"),
						obj.getString("sender_nickname"),
						obj.getString("type"),
						obj.getString("message"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result.add(site);

		}
		return result;
	}
}
