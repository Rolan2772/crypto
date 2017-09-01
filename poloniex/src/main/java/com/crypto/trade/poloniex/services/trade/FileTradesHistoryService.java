package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileTradesHistoryService implements HistoryService {

    @Autowired
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration) {
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        return loadTradesHistory(currencyPair, end.minus(historyDuration), end);
    }

    @Override
    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, LocalDateTime start, LocalDateTime end) {
        LocalDate currentDay = start.toLocalDate();
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        String fileName = "analytics/history/" + currencyPair + "/poloniex-" + currentDay + ".json";
        Path historyPath = Paths.get(fileName);
        while (historyPath.toFile().exists() && currentDay.isBefore(end.toLocalDate())) {
            log.info("Loading " + currencyPair + " " + historyPath.toFile().getName());
            try {
                trades.addAll(jsonMapper.readValue(historyPath.toFile(), new TypeReference<List<PoloniexHistoryTrade>>() {
                }));
            } catch (IOException e) {
                log.error("Failed to read history", e);
            }
            currentDay = currentDay.plusDays(1);
            historyPath = Paths.get("analytics/history/" + currencyPair + "/poloniex-" + currentDay + ".json");
        }
        return trades;
    }
}
