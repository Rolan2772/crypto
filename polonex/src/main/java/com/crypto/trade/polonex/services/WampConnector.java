package com.crypto.trade.polonex.services;

import com.crypto.trade.polonex.config.properties.PoloniexProperties;
import com.crypto.trade.polonex.dto.PoloniexTick;
import com.crypto.trade.polonex.storage.TickersStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class WampConnector {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ThreadPoolTaskExecutor ticksExecutor;
    @Autowired
    private TickersStorage tickersStorage;

    static final int eventInterval = 2000;
    int lastEventValue = 0;
    private Subscription eventPublication;
    private Subscription eventSubscription;
    private AtomicLong counter = new AtomicLong(0);


    @PostConstruct
    public void postConstruct() {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        // Build client
        final WampClient client;
        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri(poloniexProperties.getApiResources().getWsUrl())
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
                            //Thread th = new Thread(() -> {
                            ZonedDateTime time = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault());
                            ticksExecutor.submit(() -> {
                                String pair = s.arguments().get(0).asText();
                                if (pair.equals("BTC_ETH")) {
                                    log.info("BTC_ETH ({}): {} args {}", counter.addAndGet(1), time.toLocalTime(), s.arguments().toString());
                                }
                                PoloniexTick poloniexTick = new PoloniexTick(time,
                                        pair,
                                        s.arguments().get(1).asText(),
                                        s.arguments().get(2).asText(),
                                        s.arguments().get(3).asText(),
                                        s.arguments().get(4).asText(),
                                        s.arguments().get(5).asText(),
                                        s.arguments().get(6).asText(),
                                        s.arguments().get(7).asBoolean(),
                                        s.arguments().get(8).asText(),
                                        s.arguments().get(9).asText());
                                tickersStorage.addTicker(poloniexTick);
                                assert tickersStorage.getTicks().get(poloniexTick.getCurrencyPair()) == null;
                                assert !tickersStorage.getTicks().get(poloniexTick.getCurrencyPair()).contains(poloniexTick);
                                //log.info("BTC Tickers: {}", tickersStorage.getTicks().entrySet().stream().filter(e -> e.getKey().startsWith("BTC")).count());
                                //Set<PoloniexTick> eth = tickersStorage.getTicks().getOrDefault("BTC_ETH", Collections.emptySet());
                                //log.debug("BTC_ETH({}): {}", eth.size(), eth);
//                            if (poloniexTick.getCurrencyPair().equals("BTC_ETH")) {
//                                log.info("BTC_ETH ({}): args {}", counter.addAndGet(1), s.arguments().toString());
//                            }
                                //log.info("args: {}", s.arguments().toString());
                                //});
                                //th.start();
                            });
                        }, th -> log.error("Failed to subscribe on 'ticker' ", th));

                Subscription eventSubscription1 = client.makeSubscription("BTC_ETH")
                        .subscribe(s -> {
                            //log.info("BTC_ETH keyword: {}, args: {}", s.keywordArguments(), s.arguments().toString());
                        }, th -> log.error("Failed to subscribe on BTC_ETH ", th));
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
