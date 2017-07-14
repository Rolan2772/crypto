package com.crypto.trade.poloniex.services.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CsvFileWriter {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void write(String name, StringBuilder data) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(name + "_" + LocalDateTime.now().format(formatter) + ".csv"));
            writer.write(data.toString());
        } catch (IOException ioe) {
            log.error("Failed to write csv", ioe);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
                log.error("Failed to close writer.", ioe);
            }
        }
    }
}
