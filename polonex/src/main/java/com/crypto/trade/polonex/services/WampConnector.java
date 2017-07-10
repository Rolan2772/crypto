package com.crypto.trade.polonex.services;

import com.crypto.trade.polonex.config.properties.PoloniexProperties;
import com.crypto.trade.polonex.dto.PoloniexTrade;
import com.crypto.trade.polonex.storage.TickersStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import rx.Subscription;
import ws.wamp.jawampa.SubscriptionFlags;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class WampConnector {

    static final int eventInterval = 2000;
    int lastEventValue = 0;
    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ThreadPoolTaskExecutor ticksExecutor;
    @Autowired
    private TickersStorage tickersStorage;
    private Subscription eventPublication;
    private Subscription eventSubscription;
    private AtomicLong counter = new AtomicLong(0);


    //@PostConstruct
    public void postConstruct() {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        // Build client
        final WampClient client;
        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri(poloniexProperties.getApiResources().getWsApi())
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

                        eventSubscription = client.makeSubscription("BTC_ETH", SubscriptionFlags.Exact)
                                .subscribe(s -> {
                                    ticksExecutor.submit(() -> {
                                        log.info("Trade ({}): keyword: {}, args: {}", counter.addAndGet(1), s.keywordArguments(), s.arguments());
                                        for (int index = 0; s.arguments() != null && index < s.arguments().size(); index++) {
                                            JsonNode node = s.arguments().get(index);
                                            if ("newTrade".equals(node.get("type").asText())) {
                                                //log.info("Trade ({}): keyword: {}, args: {}", counter.addAndGet(1), s.keywordArguments(), node);
                                                try {
                                                    PoloniexTrade trade = objectMapper.treeToValue(node.get("data"), PoloniexTrade.class);
                                                    tickersStorage.addTrade("BTC_ETH", trade);
                                                } catch (JsonProcessingException e) {
                                                    log.error("Failed to convert node '" + node + "' to trade.", e);
                                                }
                                            }
                                        }
                                    });
                                }, th -> log.error("Failed to subscribe on 'ticker' ", th));

                    }
                },
                t -> System.out.println("Session ended with error " + t), () -> System.out.println("Session ended normally"));
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
