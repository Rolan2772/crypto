package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.crypto.trade.poloniex.services.trade.PoloniexRequestConstants.*;

public class PoloniexRequestHelper {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private SignatureGenerator signatureGenerator;

    public String createRequestBody(Map<String, Object> params) {
        StringBuilder body = new StringBuilder();
        params.computeIfAbsent(NONCE, key -> Instant.now().toEpochMilli());
        params.computeIfAbsent(METHOD, key -> "GET");
        String paramsStr = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        body.append(paramsStr);
        return body.toString();
    }

    public HttpHeaders createRequestHeaders(String body) {
        String sign = signatureGenerator.sign(body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(KEY, properties.getSecret().getKey());
        headers.set(SIGN, sign);
        return headers;
    }

    public HttpEntity<String> createRequest(Map<String, Object> params) {
        String body = createRequestBody(params);
        HttpHeaders headers = createRequestHeaders(body);
        return new HttpEntity<>(body, headers);
    }
}
