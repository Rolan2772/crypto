package com.crypto.trade.poloniex.services.connection;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LoadHistoryService {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private RestTemplate restTemplate;

    public List<PoloniexHistoryTrade> loadTradesHistory() {
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        int count = 72;
        int inc = 6;
        for (int i = count; i > inc; i -= inc) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("currencyPair", "BTC_ETH");
            Instant from = Instant.now().minus(i, ChronoUnit.HOURS);
            parameters.put("startTime", from.getEpochSecond());
            Instant to = Instant.now().minus(i - inc, ChronoUnit.HOURS);
            parameters.put("endTime", to.getEpochSecond());
            log.info("Requesting time: {} - {}", from, to);
            ResponseEntity<List<PoloniexHistoryTrade>> response = restTemplate.exchange(poloniexProperties.getApiResources().getTradeHistoryResource(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<PoloniexHistoryTrade>>() {
                    }, parameters);
            trades.addAll(response.getBody());

        }
        //tickersStorage.addTradesHistory("BTC_ETH", items);
        return trades;
    }
}
