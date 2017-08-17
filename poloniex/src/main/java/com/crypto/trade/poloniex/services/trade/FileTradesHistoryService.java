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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileTradesHistoryService implements HistoryService {

    @Autowired
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration) {
        List<PoloniexHistoryTrade> trades = new ArrayList<>();
        String fileName = "analytics/poloniex.json";
        int index = 0;
        Path historyPath = Paths.get(fileName);
        while (historyPath.toFile().exists()) {
            try {
                trades.addAll(jsonMapper.readValue(historyPath.toFile(), new TypeReference<List<PoloniexHistoryTrade>>() {
                }));
            } catch (IOException e) {
                log.error("Failed to read history.", e);
            }
            index += 1;
            historyPath = Paths.get(fileName + "." + index);
        }
        return trades;
    }
}
