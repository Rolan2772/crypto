package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.services.utils.SyncUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CopyTradesHistoryService implements HistoryService {

    public static final Duration MAXIMUM_PER_REQUEST = Duration.ofHours(3);

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SyncUtils syncUtils;
    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration) {
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        return loadTradesHistory(currencyPair, end.minus(historyDuration), end);
    }

    @Override
    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, LocalDateTime start, LocalDateTime end) {
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        //LocalDate fromDate = LocalDate.of(2017, Month.JANUARY, 1);
        //LocalDate toDate = LocalDate.of(2017, Month.AUGUST, 28);
        Instant now = end.toInstant(ZoneOffset.UTC);
        Instant from = start.toInstant(ZoneOffset.UTC);
        SortedSet<PoloniexHistoryTrade> history = new TreeSet<>(Comparator.comparing(PoloniexHistoryTrade::getDate)
                .thenComparing(PoloniexHistoryTrade::getGlobalTradeId));
        log.info("Loading trades history from {} to {}", ZonedDateTime.ofInstant(from, ZoneOffset.UTC).toLocalDateTime(), ZonedDateTime.ofInstant(now, ZoneOffset.UTC).toLocalDateTime());
        while (from.compareTo(now) < 0) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("currencyPair", currencyPair);
            parameters.put("startTime", from.getEpochSecond());
            Instant to = from.plus(MAXIMUM_PER_REQUEST).minusSeconds(1);
            parameters.put("endTime", to.getEpochSecond());
            try {
                log.info("Requesting time: {} - {}", from, to);
                ResponseEntity<List<PoloniexHistoryTrade>> response = restTemplate.exchange(properties.getApi().getTradeHistoryUrl(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<PoloniexHistoryTrade>>() {
                        }, parameters);
                history.addAll(response.getBody());
                LocalDate currentPeriodDay = LocalDateTime.ofInstant(from, ZoneOffset.UTC).toLocalDate();
                LocalDate nextPeriodDay = LocalDateTime.ofInstant(from.plus(MAXIMUM_PER_REQUEST), ZoneOffset.UTC).toLocalDate();
                if (currentPeriodDay.isBefore(nextPeriodDay)) {
                    Path pathToFile = Paths.get("analytics/history/" + currencyPair + "/poloniex-" + currentPeriodDay + ".json");
                    try {
                        Files.createDirectories(pathToFile.getParent());
                        objectMapper.writeValue(pathToFile.toFile(), history);
                    } catch (IOException e) {
                        log.error("Failed to write " + currentPeriodDay, e);
                    }
                    history.clear();
                }
                from = from.plus(MAXIMUM_PER_REQUEST);
            } catch (RuntimeException ex) {
                syncUtils.sleep(TimeUnit.SECONDS.toMillis(5));
                log.error("Failed to request data.", ex);
            }
        }
        log.info("Finished to copy data");
        return trades;
    }
}
