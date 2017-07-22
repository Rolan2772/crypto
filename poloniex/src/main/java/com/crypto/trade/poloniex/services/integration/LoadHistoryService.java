package com.crypto.trade.poloniex.services.integration;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.bots.SimpleTradingBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LoadHistoryService {

    public static final Duration MAXIMUM_PER_REQUEST = Duration.ofHours(6);

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private RestTemplate restTemplate;

    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration) {
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        Instant from = Instant.now().minus(historyDuration);
        Instant to = Instant.now();
        log.info("Loading trades history from {} to {}",
                ZonedDateTime.ofInstant(from, SimpleTradingBot.GMT0).toLocalDateTime(),
                ZonedDateTime.ofInstant(to, SimpleTradingBot.GMT0).toLocalDateTime());
        while (from.compareTo(to) < 0) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("currencyPair", currencyPair);
            parameters.put("startTime", from.getEpochSecond());
            parameters.put("endTime", to.getEpochSecond());
            log.info("Requesting time: {} - {}", from, to);
            ResponseEntity<List<PoloniexHistoryTrade>> response = restTemplate.exchange(properties.getApi().getTradeHistoryUrl(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<PoloniexHistoryTrade>>() {
                    }, parameters);
            trades.addAll(response.getBody());
            from = from.plus(MAXIMUM_PER_REQUEST);
        }
        return trades;
    }
}
