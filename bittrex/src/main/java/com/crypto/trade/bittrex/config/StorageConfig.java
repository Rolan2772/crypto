package com.crypto.trade.bittrex.config;

import com.crypto.trade.bittrex.storage.DataStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public DataStorage currenciesStorage() {
        return new DataStorage();
    }
}
