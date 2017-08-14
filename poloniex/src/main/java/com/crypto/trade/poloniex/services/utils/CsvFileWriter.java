package com.crypto.trade.poloniex.services.utils;

import com.crypto.trade.poloniex.services.export.ExportData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class CsvFileWriter {

    public void write(ExportData exportData) {
        write(exportData, false);
    }

    public void write(ExportData exportData, boolean append) {
        String path = exportData.getCurrencyPair() + "/" + exportData.getNamePrefix() + ".csv";
        write(exportData.getData(), path, append);
    }

    public void write(String data, String path, boolean append) {
        try {
            Path pathToFile = Paths.get(path);
            Files.createDirectories(pathToFile.getParent());

            try (FileWriter fileWriter = new FileWriter(pathToFile.toFile(), append);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(data);
            }

        } catch (IOException e) {
            log.error("Failed to write file: " + path, e);
        }
    }
}
