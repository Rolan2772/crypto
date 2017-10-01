package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.ApiSecretProperties;
import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.crypto.trade.poloniex.services.analytics.CurrencyPair.BTC_ETH;
import static com.crypto.trade.poloniex.services.trade.PoloniexRequestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class PoloniexRequestHelperTest {

    public static final String SECRET_KEY = "5FNMRS2B-FV6ZS5OV-BJTQP0LW-R34IO5FE";

    @Mock
    private PoloniexProperties properties;
    @Mock
    private ApiSecretProperties secret;
    @Mock
    private SignatureGenerator signatureGenerator;

    @Spy
    @InjectMocks
    private PoloniexRequestHelper requestHelper;

    @Before
    public void before() {
        given(properties.getSecret()).willReturn(secret);
        given(secret.getKey()).willReturn(SECRET_KEY);
        given(signatureGenerator.sign(any())).will(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void createRequestHeaders() {
        String body = "nonce=1506887098719&method=GET&command=returnBalances";

        HttpHeaders httpHeaders = requestHelper.createRequestHeaders(body);

        assertEquals(SECRET_KEY, httpHeaders.getFirst(KEY));
        assertEquals(body, httpHeaders.getFirst(SIGN));
    }

    @Test
    public void createRequestBody() {
        Map<String, Object> params = new HashMap<>();
        params.put(COMMAND, "buy");
        params.put(CURRENCY_PAIR, BTC_ETH);
        params.put(RATE, BigDecimal.valueOf(0.0750203));
        params.put(AMOUNT, BigDecimal.valueOf(0.1));

        String body = requestHelper.createRequestBody(params);

        Map<String, String> bodyParams = extractParameters(body);
        assertTrue(bodyParams.containsKey(NONCE));
        assertEquals("GET", bodyParams.getOrDefault(METHOD, "No value present"));
        assertEquals("buy", bodyParams.getOrDefault(COMMAND, "No value present"));
        assertEquals("BTC_ETH", bodyParams.getOrDefault(CURRENCY_PAIR, "No value present"));
        assertEquals("0.0750203", bodyParams.getOrDefault(RATE, "No value present"));
        assertEquals("0.1", bodyParams.getOrDefault(AMOUNT, "No value present"));
    }

    private Map<String, String> extractParameters(String body) {
        return Pattern.compile("&")
                .splitAsStream(body)
                .map(param -> param.split("="))
                .collect(Collectors.toMap(param -> param[0], param -> param[1]));
    }

    @Test
    public void createRequest() {
        Map<String, Object> params = new HashMap<>();
        params.put(COMMAND, "returnBalances");

        HttpEntity<String> request = requestHelper.createRequest(params);

        assertEquals(request.getBody(), request.getHeaders().getFirst(SIGN));
        Map<String, String> bodyParams = extractParameters(request.getBody());
        assertTrue(bodyParams.containsKey(NONCE));
        assertEquals("GET", bodyParams.getOrDefault(METHOD, "No value present"));
        assertEquals("returnBalances", bodyParams.getOrDefault(COMMAND, "No value present"));
    }
}
