package com.crypto.trade.bittrex.services;

import com.crypto.trade.bittrex.config.BittrexApiResources;
import com.crypto.trade.bittrex.dto.BittrexCurrenciesResponse;
import com.crypto.trade.bittrex.dto.BittrexCurrency;
import com.crypto.trade.bittrex.dto.BittrexMarketItem;
import com.crypto.trade.bittrex.dto.BittrexMarketsResponse;
import com.crypto.trade.bittrex.storage.DataStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class RestDataConsumerService implements DataConsumerService {

    @Autowired
    private BittrexApiResources bittrexApiResources;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DataStorage dataStorage;

    @Override
    public void loadCurrencies() {
        BittrexCurrenciesResponse currenciesResponse = restTemplate.getForObject(bittrexApiResources.getCurrenciesUrl(), BittrexCurrenciesResponse.class);
        log.info(currenciesResponse.toString());
        currenciesResponse.getResult()
                .stream()
                .filter(BittrexCurrency::isActive)
                .forEach(ccy -> dataStorage.addCcy(ccy));

        BittrexMarketsResponse bittrexMarketsResponse = restTemplate.getForObject(bittrexApiResources.getMarketsUrl(), BittrexMarketsResponse.class);
        log.info(bittrexMarketsResponse.toString());
        bittrexMarketsResponse.getResult()
                .stream()
                .filter(BittrexMarketItem::isActive)
                .forEach(marketItem -> dataStorage.addMarket(marketItem));

        log.info("Bittrex data has been loaded");
    }

    @PostConstruct
    public void postConstruct() {
        loadCurrencies();
    }
}
