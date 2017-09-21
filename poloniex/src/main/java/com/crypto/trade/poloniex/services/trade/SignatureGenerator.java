package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class SignatureGenerator {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String HMAC_SHA512 = "HmacSHA512";

    @Autowired
    private PoloniexProperties properties;
    private final Mac mac;

    public SignatureGenerator() {
        try {
            this.mac = Mac.getInstance(HMAC_SHA512);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to create " + HMAC_SHA512 + " MAC.", e);
        }
    }

    public String sign(String message) {
        try {
            String key = properties.getSecret().getSignature();
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(DEFAULT_ENCODING), HMAC_SHA512);
            mac.init(keySpec);
            return toHexString(mac.doFinal(message.getBytes(DEFAULT_ENCODING)));
        } catch (InvalidKeyException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String toHexString(byte[] bytes) {
        StringBuilder formatted = new StringBuilder();
        try (Formatter formatter = new Formatter(formatted)) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
        }
        return formatted.toString();
    }
}
