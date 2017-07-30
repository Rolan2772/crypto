package com.crypto.trade.poloniex.services.export;


import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import lombok.Value;

@Value
public class ExportData {

    private static String OS = System.getProperty("os.name").toLowerCase();

    private CurrencyPair currencyPair;
    private String namePrefix;
    private String data;
    private OsType osType;

    public ExportData(CurrencyPair currencyPair, String namePrefix, StringBuilder data) {
        this.currencyPair = currencyPair;
        this.namePrefix = namePrefix;
        this.data = data.toString();
        this.osType = OS.contains("win") ? OsType.WIN : OsType.UNIX;
    }

    public ExportData(CurrencyPair currencyPair, String namePrefix, StringBuilder data, OsType osType) {
        this.currencyPair = currencyPair;
        this.namePrefix = namePrefix;
        this.data = data.toString();
        this.osType = osType;
    }

    public String getData() {
        return OsType.WIN == osType ? data.replaceAll("\\,", "\\;").replaceAll("\\.", "\\,") : data;
    }
}
