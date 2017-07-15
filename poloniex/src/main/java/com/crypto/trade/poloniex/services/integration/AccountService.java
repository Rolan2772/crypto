package com.crypto.trade.poloniex.services.integration;

import com.crypto.trade.poloniex.config.properties.ApiSecretProperties;
import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.utils.EncodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Formatter;

@Slf4j
@Service
public class AccountService {

    private String key = "ZFNMRR2B-FV6US5OV-BJTQP0LW-ROHIO5FE";
    private String signature = "8662e56994002c90b60e98e1e8e55ec1acbcf049a330ab0b01080f52f6b16c4bd83eda3b7dd50918a266ed3322e0191db4dcd288ecf3eac8d8692d73e28824f5";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ApiSecretProperties apiSecretProperties;

    public AccountBalance requestBalance() {
        String body = "nonce=" + Instant.now().toEpochMilli() + "&method=GET&command=returnBalances";
        String sign = EncodeUtils.hmacSha512(body, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Key", key);
        headers.set("Sign", sign);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<AccountBalance> response = restTemplate.postForEntity(poloniexProperties.getApiResources().getTradingApi(), requestEntity, AccountBalance.class);
        log.info("Account balance: {}", response.getBody());
        return response.getBody();
    }



}
