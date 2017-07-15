package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.integration.AccountBalance;
import com.crypto.trade.poloniex.services.integration.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountApiController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/balance")
    public AccountBalance getBalance() {
        return accountService.requestBalance();
    }
}
