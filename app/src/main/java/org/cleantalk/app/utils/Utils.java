package org.cleantalk.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import hirondelle.date4j.DateTime;
import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.RequestModel;
import org.cleantalk.app.model.Site;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public enum ToastType {Error, Info}

    public static String getDeviceId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * Decrypt value with some key and initialization vector used AES128 Decrypting algorithm. Encrypted value encode to BASE64 format.
     *
     * @param value                - source value
     * @param key                  - encryption key
     * @param initialisationVector - initialization vector
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
     * @param value                - source value
     * @param key                  - encryption key
     * @param initialisationVector - initialization vector
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
     * @param oldArray the old array, to be reallocated.
     * @param newSize  the new array size.
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

    public static long getNow(TimeZone timeZone) {
        return DateTime
                .now(TimeZone.getTimeZone("GMT+0"))
                .getMilliseconds(timeZone) / 1000;
    }

    public static long getTodayTimestamp(TimeZone timeZone) {
        return DateTime
                .today(TimeZone.getTimeZone("GMT+0"))
                .getMilliseconds(timeZone) / 1000;
    }

    public static long getYesterdayTimestamp(TimeZone timeZone) {
        return DateTime
                .today(TimeZone.getTimeZone("GMT+0"))
                .minusDays(1)
                .getMilliseconds(timeZone) / 1000;
    }

    public static long getWeekAgoTimestamp(TimeZone timeZone) {
        return DateTime
                .today(TimeZone.getTimeZone("GMT+0"))
                .minusDays(7)
                .getMilliseconds(timeZone) / 1000;
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
                        obj.getJSONObject("today").getInt("allow"),
                        obj.getJSONObject("today").getInt("spam"),
                        obj.getJSONObject("yesterday").getInt("allow"),
                        obj.getJSONObject("yesterday").getInt("spam"),
                        obj.getJSONObject("week").getInt("allow"),
                        obj.getJSONObject("week").getInt("spam"),
                        obj.getString("auth_key")
                );
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            result.add(site);

        }
        return result;
    }

    public static List<RequestModel> parseRequests(Context context,
                                                   JSONArray array,
                                                   long endTo_,
                                                   SharedPreferences preferences) {
        List<RequestModel> result = new ArrayList<RequestModel>();
        int len = array.length();

        for (int i = 0; i < len; i++) {
            JSONObject obj = null;
            try {
                obj = array.getJSONObject(i);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                if (endTo_ > 0) {
                    String str_date = obj.getString("datetime");
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    formatter.setTimeZone(ServiceApi.getInstance(context).getTimezone());
                    Date date = formatter.parse(str_date);
                    if (date.getTime() / 1000 > endTo_) {
                        continue;
                    }
                }

                String requestId = obj.getString("request_id");
                RequestModel request = new RequestModel(
                        requestId,
                        obj.getInt("allow") == 1,
                        obj.getString("datetime"),
                        obj.getString("sender_email"),
                        obj.getString("sender_nickname"),
                        obj.getString("type"),
                        obj.getBoolean("show_approved"),
                        isInProgress(requestId, preferences),
                        obj.getString("message"),
                        obj.optInt("approved", -1));
                result.add(request);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    private static boolean isInProgress(String requestId, SharedPreferences preferences) {
        if (preferences.contains(requestId)) {
            if (isLessThan5Minutes(preferences.getLong(requestId, 0L))) {
                return true;
            } else {
                preferences.edit().remove(requestId).apply();
                return false;
            }
        }
        return false;
    }

    private static boolean isLessThan5Minutes(Long timestamp) {
        Date now = new Date();
        return ((now.getTime() - timestamp) < 60 * 1000);
    }

    public static Toast makeToast(Context context, String text, ToastType type) {
        TextView textview = (TextView) LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        textview.setText(text);
        switch (type) {
            case Error:
                textview.setBackgroundResource(R.drawable.toast_error_bg);
                break;
            case Info:
                textview.setBackgroundResource(R.drawable.toast_bg);
                break;
        }
        Toast toast = new Toast(context);
        toast.setView(textview);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        return toast;
    }
}
