package com.crypto.trade.poloniex.services.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class EncodeUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String HMAC_SHA512 = "HmacSHA512";

    public static String hmacSha512(String value, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(DEFAULT_ENCODING),
                    HMAC_SHA512);

            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(keySpec);
            return toHexString(mac.doFinal(value.getBytes(DEFAULT_ENCODING)));

        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
