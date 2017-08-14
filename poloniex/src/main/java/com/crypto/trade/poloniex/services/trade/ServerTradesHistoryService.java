package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.services.utils.SyncUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ServerTradesHistoryService implements HistoryService{

    public static final Duration MAXIMUM_PER_REQUEST = Duration.ofHours(6);

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SyncUtils syncUtils;

    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration) {
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        Instant from = Instant.now().minus(historyDuration);
        Instant now = Instant.now();
        log.info("Loading trades history from {} to {}", ZonedDateTime.ofInstant(from, ZoneOffset.UTC).toLocalDateTime(), ZonedDateTime.ofInstant(now, ZoneOffset.UTC).toLocalDateTime());
        while (from.compareTo(now) < 0) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("currencyPair", currencyPair);
            parameters.put("startTime", from.getEpochSecond());
            Instant to = from.plus(MAXIMUM_PER_REQUEST);
            parameters.put("endTime", to.getEpochSecond());
            try {
                log.info("Requesting time: {} - {}", from, to);
                ResponseEntity<List<PoloniexHistoryTrade>> response = restTemplate.exchange(properties.getApi().getTradeHistoryUrl(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<PoloniexHistoryTrade>>() {
                        }, parameters);
                trades.addAll(response.getBody());
                from = from.plus(MAXIMUM_PER_REQUEST).plusSeconds(1);
            } catch (RuntimeException ex) {
                syncUtils.sleep(TimeUnit.SECONDS.toMillis(5));
                log.error("Failed to request data.", ex);
            }
        }
        return trades;
    }
}
