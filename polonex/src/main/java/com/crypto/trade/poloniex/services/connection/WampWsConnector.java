package com.crypto.trade.poloniex.services.connection;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PolonexTradeHistoryItem;
import com.crypto.trade.poloniex.dto.PoloniexTick;
import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.storage.TickersStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class WampWsConnector implements WsConnector {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ThreadPoolTaskExecutor ticksExecutor;
    @Autowired
    private TickersStorage tickersStorage;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

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
                            ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC);
                            if (s.arguments().get(0).asText().equals("BTC_ETH")) {
                                ticksExecutor.submit(() -> {
                                    try {
                                        String pair = s.arguments().get(0).asText();
                                        if (pair.equals("BTC_ETH")) {
                                            log.debug("BTC_ETH ({}): {} args {}", counter.addAndGet(1), time.toLocalTime(), s.arguments().toString());
                                        }
                                        PoloniexTick poloniexTick = new PoloniexTick(0L,
                                                time,
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
                                        tickersStorage.addTick(poloniexTick);
                                    } catch (RuntimeException ex) {
                                        log.error("fsdfs", ex);
                                    }
                                });
                            }
                        }, th -> log.error("Failed to subscribe on 'ticker' ", th));

                List<PolonexTradeHistoryItem> items = new ArrayList<>();

                int count = 72;
                int inc = 6;
                for (int i = count; i > inc; i -= inc) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("currencyPair", "BTC_ETH");
                    Instant from = Instant.now().minus(i, ChronoUnit.HOURS);
                    parameters.put("startTime", from.getEpochSecond());
                    Instant to = Instant.now().minus(i - inc, ChronoUnit.HOURS);
                    parameters.put("endTime", to.getEpochSecond());
                    log.info("Requesting time: {} - {}", from, to);
                    ResponseEntity<List<PolonexTradeHistoryItem>> response = restTemplate.exchange(poloniexProperties.getApiResources().getTradeHistoryUrl(),
                            HttpMethod.GET, null, new ParameterizedTypeReference<List<PolonexTradeHistoryItem>>() {
                            }, parameters);
                    items.addAll(response.getBody());

                }
               tickersStorage.addTradesHistory("BTC_ETH", items);

                client.makeSubscription("BTC_ETH")
                        .subscribe(s -> {
                            //log.info("BTC_ETH keyword: {}, args: {}", s.keywordArguments(), s.arguments().toString());
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
