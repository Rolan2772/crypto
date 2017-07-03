package com.crypto.trade.polonex.services;

import com.crypto.trade.polonex.dto.Ticker;
import com.crypto.trade.polonex.storage.TickersStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WampConnector {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TickersStorage tickersStorage;

    private Subscription eventPublication;
    private Subscription eventSubscription;

    static final int eventInterval = 2000;
    int lastEventValue = 0;


    @PostConstruct
    public void postConstruct() {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        // Build client
        final WampClient client;
        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri("wss://api.poloniex.com")
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(5, TimeUnit.SECONDS);
            client = builder.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to open ws connection", ex);
        }


        client.statusChanged().subscribe(t1 -> {
            System.out.println("Session status changed to " + t1);

            if (t1 instanceof WampClient.ConnectedState) {

                eventSubscription = client.makeSubscription("ticker")
                        .subscribe(s -> {
                            Ticker ticker = new Ticker(Instant.now(),
                                    s.arguments().get(0).asText(),
                                    new BigDecimal(s.arguments().get(1).asText()),
                                    new BigDecimal(s.arguments().get(2).asText()),
                                    new BigDecimal(s.arguments().get(3).asText()),
                                    new BigDecimal(s.arguments().get(4).asText()),
                                    new BigDecimal(s.arguments().get(5).asText()),
                                    new BigDecimal(s.arguments().get(6).asText()),
                                    s.arguments().get(7).asBoolean(),
                                    new BigDecimal(s.arguments().get(8).asText()),
                                    new BigDecimal(s.arguments().get(9).asText()));
                            tickersStorage.addTicker(ticker);
                            //log.info("BTC Tickers: {}", tickersStorage.getTickers().entrySet().stream().filter(e -> e.getKey().startsWith("BTC")).count());
                            Set<Ticker> eth = tickersStorage.getTickers().getOrDefault("BTC_ETH", Collections.emptySet());
                            //log.debug("BTC_ETH({}): {}", eth.size(), eth);

                            if (ticker.getCurrencyPair().equals("BTC_ETH")) {
                                log.info("{}: {}", ticker.getLast(), ticker.getTime());
                            }
                        }, th -> {
                            log.error("Failed to subscribe on 'ticker' " + th);
                        });
            }
        }, t -> System.out.println("Session ended with error " + t), () -> System.out.println("Session ended normally"));
        client.open();

        // Publish an event regularly
        /*eventPublication = Schedulers.computation().createWorker().schedulePeriodically(() -> {
            client.publish("ticker " + lastEventValue);
            lastEventValue++;
        }, eventInterval, eventInterval, TimeUnit.MILLISECONDS);*/
    }

    @PreDestroy
    public void preDestroy() {
        if (eventSubscription != null) {
            eventSubscription.unsubscribe();
        }
        if (eventPublication != null) {
            eventPublication.unsubscribe();
        }
    }
}
