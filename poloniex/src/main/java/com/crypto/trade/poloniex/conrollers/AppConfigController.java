package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.bots.SimplePoloniexBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.DeploymentException;
import java.io.IOException;

@RestController
@RequestMapping("/config")
public class AppConfigController {

    @Autowired
    private SimplePoloniexBot simplePoloniexBot;

    @GetMapping("/reload/strategies")
    public void reloadStrategies() throws IOException, DeploymentException {
    }

}
