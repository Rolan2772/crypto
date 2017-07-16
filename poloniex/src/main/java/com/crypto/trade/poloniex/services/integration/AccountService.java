package com.crypto.trade.poloniex.services.integration;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PoloniexProperties poloniex;
    @Autowired
    private PoloniexRequestHelper requestHelper;

    public AccountBalance requestBalance() {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "returnBalances");
        HttpEntity<String> requestEntity = requestHelper.createRequest(params);

        ResponseEntity<AccountBalance> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, AccountBalance.class);
        log.info("Account balance: {}", response.getBody());
        return response.getBody();
    }
}
