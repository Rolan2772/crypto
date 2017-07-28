package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.utils.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public class PoloniexRequestHelper {

    @Autowired
    private PoloniexProperties poloniex;

    public String createRequestBody(Map<String, Object> params) {
        StringBuilder body = new StringBuilder();
        params.computeIfAbsent("nonce", key -> Instant.now().toEpochMilli());
        params.computeIfAbsent("method", key -> "GET");
        String paramsStr = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        body.append(paramsStr);
        return body.toString();
    }

    public HttpHeaders createRequestHeaders(String body) {
        String sign = HashUtils.hmacSha512(body, poloniex.getSecret().getSignature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Key", poloniex.getSecret().getKey());
        headers.set("Sign", sign);
        return headers;
    }

    public HttpEntity<String> createRequest(Map<String, Object> params) {
        String body = createRequestBody(params);
        HttpHeaders headers = createRequestHeaders(body);
        return new HttpEntity<>(body, headers);
    }
}
