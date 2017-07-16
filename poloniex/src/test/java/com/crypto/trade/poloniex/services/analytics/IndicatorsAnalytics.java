package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.export.AnalyticsExportService;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.TickersStorage;
import com.opencsv.CSVReader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndicatorsAnalytics {

    public static void main(String[] args) {
        TickersStorage tickersStorage = new TickersStorage();
        loadTicks(tickersStorage);
        CsvFileWriter csvFileWriter = new CsvFileWriter();
        AnalyticsExportService analyticsExportService = new AnalyticsExportService();
        ReflectionTestUtils.setField(analyticsExportService, "tickersStorage", tickersStorage);
        ReflectionTestUtils.setField(analyticsExportService, "csvFileWriter", csvFileWriter);

        analyticsExportService.exportData();
    }

    private static void loadTicks(TickersStorage tickersStorage) {
        InputStream stream = StrategiesBuilder.class.getClassLoader().getResourceAsStream("ticks/poloniex_ticks_2017-07-12.csv");
        CSVReader csvReader = null;
        List<String[]> lines = null;
        try {
            csvReader = new CSVReader(new InputStreamReader(stream, Charset.forName("UTF-8")), ',');
            lines = csvReader.readAll();
            lines.remove(0); // Removing header line
        } catch (IOException ioe) {
            Logger.getLogger(StrategiesBuilder.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", ioe);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ioe) {
                }
            }
        }

        if ((lines != null) && !lines.isEmpty()) {
            for (String[] tradeLine : lines) {
                ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(tradeLine[1]) * 1000), ZoneId.of("GMT+0"));
                PoloniexTrade trade = new PoloniexTrade(0L, time, tradeLine[2], "", "", "");
                tickersStorage.addTrade(CurrencyPair.BTC_ETH, trade);
            }
        }
    }

    private static void loadTicks1(TickersStorage tickersStorage) {
        InputStream stream = StrategiesBuilder.class.getClassLoader().getResourceAsStream("ticks/test.csv");
        CSVReader csvReader = null;
        List<String[]> lines = null;
        try {
            csvReader = new CSVReader(new InputStreamReader(stream, Charset.forName("UTF-8")), ',');
            lines = csvReader.readAll();
            lines.remove(0); // Removing header line
        } catch (IOException ioe) {
            Logger.getLogger(StrategiesBuilder.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", ioe);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ioe) {
                }
            }
        }

        ZonedDateTime tickTime = ZonedDateTime.now(ZoneId.of("GMT+0"));
        if ((lines != null) && !lines.isEmpty()) {
            for (String[] tradeLine : lines) {
                PoloniexTrade trade = new PoloniexTrade(0L, tickTime, tradeLine[0], "", "", "");
                tickersStorage.addTrade(CurrencyPair.BTC_ETH, trade);
                tickTime = tickTime.plusMinutes(1);
            }
        }
    }
}
