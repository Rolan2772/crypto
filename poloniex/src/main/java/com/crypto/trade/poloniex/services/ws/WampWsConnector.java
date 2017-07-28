package com.crypto.trade.poloniex.services.ws;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.storage.TradesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class WampWsConnector implements WsConnector {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ThreadPoolTaskExecutor tradesExecutor;
    @Autowired
    private TradesStorage tradesStorage;

    private Subscription eventSubscription;
    private AtomicLong counter = new AtomicLong(0);

    @Override
    public void connect() {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        // Build client
        final WampClient client;
        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri(poloniexProperties.getApi().getWsApi())
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
                            ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC);
                            if (s.arguments().get(0).asText().equals(CurrencyPair.BTC_ETH.name())) {
                                tradesExecutor.submit(() -> {
                                    try {
                                        String pair = s.arguments().get(0).asText();
                                        if (pair.equals(CurrencyPair.BTC_ETH.name())) {
                                            log.debug("BTC_ETH ({}): {} args {}", counter.addAndGet(1), time.toLocalTime(), s.arguments().toString());
                                        }
                                        PoloniexTrade trade = new PoloniexTrade(0L,
                                                time,
                                                pair,
                                                s.arguments().get(1).asText(),
                                                "", "");
                                        tradesStorage.addTrade(CurrencyPair.BTC_ETH, trade);
                                    } catch (RuntimeException ex) {
                                        log.error("fsdfs", ex);
                                    }
                                });
                            }
                        }, th -> log.error("Failed to subscribe on 'ticker' ", th));

                client.makeSubscription(CurrencyPair.BTC_ETH.name())
                        .subscribe(s -> {
                            log.info("BTC_ETH keyword: {}, args: {}", s.keywordArguments(), s.arguments().toString());
                        }, th -> log.error("Failed to subscribe on BTC_ETH ", th));
            }
        }, t -> System.out.println("Session ended with error " + t), () -> System.out.println("Session ended normally"));
        client.open();
    }

    @Override
    public void closeConnection() {
        if (eventSubscription != null) {
            eventSubscription.unsubscribe();
        }
    }
}
